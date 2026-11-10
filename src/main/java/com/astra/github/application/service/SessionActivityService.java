package com.astra.github.application.service;

import com.astra.github.application.port.in.GetSessionActivityQuery;
import com.astra.github.application.port.in.SessionActivityResponse;
import com.astra.github.application.port.out.LoadCommitsPort;
import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.application.port.out.LoadSyncedDataPort;
import com.astra.github.domain.CommitInfo;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.SyncedRepository;
import com.astra.shared.exception.ConflictException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SessionActivityService implements GetSessionActivityQuery {

    private static final Duration CACHE_TTL = Duration.ofSeconds(90);
    private static final int MAX_REPOS_TO_SCAN = 8;

    private record CacheEntry(SessionActivityResponse value, Instant expiresAt) {
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final LoadConnectionPort loadConnectionPort;
    private final LoadSyncedDataPort loadSyncedDataPort;
    private final LoadCommitsPort loadCommitsPort;
    private final AccessTokens accessTokens;
    private final Clock clock;

    SessionActivityService(LoadConnectionPort loadConnectionPort, LoadSyncedDataPort loadSyncedDataPort,
                           LoadCommitsPort loadCommitsPort, AccessTokens accessTokens, Clock clock) {
        this.loadConnectionPort = loadConnectionPort;
        this.loadSyncedDataPort = loadSyncedDataPort;
        this.loadCommitsPort = loadCommitsPort;
        this.accessTokens = accessTokens;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = ConflictException.class)
    public SessionActivityResponse activityFor(UUID userId, OffsetDateTime from, OffsetDateTime to,
                                               UUID preferredRepositoryId) {
        Optional<GitHubConnection> connection = loadConnectionPort.loadByUser(userId);
        if (connection.isEmpty()) {
            return new SessionActivityResponse(false, List.of(), null, null);
        }
        String cacheKey = userId + "|" + preferredRepositoryId + "|" + from + "|" + to;
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(clock.instant())) {
            return cached.value();
        }
        String accessToken = accessTokens.validFor(connection.get());
        SessionActivityResponse result = preferredRepositoryId != null
                ? forRepository(userId, accessToken, preferredRepositoryId, from, to)
                : acrossRepositories(userId, accessToken, from, to);
        cache.put(cacheKey, new CacheEntry(result, clock.instant().plus(CACHE_TTL)));
        return result;
    }

    private SessionActivityResponse forRepository(UUID userId, String accessToken, UUID repositoryId,
                                                  OffsetDateTime from, OffsetDateTime to) {
        return loadSyncedDataPort.repository(repositoryId)
                .filter(r -> r.belongsTo(userId))
                .map(r -> new SessionActivityResponse(true, loadCommitsPort.commits(accessToken, r, from, to),
                        r.id(), r.fullName()))
                .orElseGet(() -> new SessionActivityResponse(true, List.of(), null, null));
    }

    private SessionActivityResponse acrossRepositories(UUID userId, String accessToken, OffsetDateTime from,
                                                       OffsetDateTime to) {
        SyncedRepository bestMatch = null;
        List<CommitInfo> bestCommits = List.of();
        for (SyncedRepository repository : loadSyncedDataPort.repositoriesByActivity(userId).stream()
                .limit(MAX_REPOS_TO_SCAN).toList()) {
            List<CommitInfo> commits = loadCommitsPort.commits(accessToken, repository, from, to);
            if (commits.size() > bestCommits.size()) {
                bestMatch = repository;
                bestCommits = commits;
            }
        }
        return bestMatch == null
                ? new SessionActivityResponse(true, List.of(), null, null)
                : new SessionActivityResponse(true, bestCommits, bestMatch.id(), bestMatch.fullName());
    }
}
