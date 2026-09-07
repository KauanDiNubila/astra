package com.astra.github;

import com.astra.github.dto.GitHubCommitInfo;
import com.astra.github.dto.SessionGitHubActivity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Chamadas ao vivo pra atividade dentro de uma janela de tempo especifica
// (ex: a duracao de uma sessao) - dado que nao vale a pena persistir commit
// por commit, mas tambem nao deve martelar a API do GitHub a cada expandir
// de painel. Cache in-process com TTL curto absorve isso sem precisar de
// dependencia nova (Caffeine/Redis).
@Service
public class GitHubLiveActivityService {

    private static final Duration CACHE_TTL = Duration.ofSeconds(90);
    private static final int MAX_REPOS_TO_SCAN = 8;
    private static final int MAX_COMMITS_PER_REPO = 30;

    private record CacheEntry(SessionGitHubActivity value, Instant expiresAt) {
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private final GitHubConnectionRepository connectionRepository;
    private final GitHubRepositoryRepository repositoryRepository;
    private final GitHubConnectionService connectionService;
    private final GitHubApiClient apiClient;

    public GitHubLiveActivityService(GitHubConnectionRepository connectionRepository,
            GitHubRepositoryRepository repositoryRepository, GitHubConnectionService connectionService,
            GitHubApiClient apiClient) {
        this.connectionRepository = connectionRepository;
        this.repositoryRepository = repositoryRepository;
        this.connectionService = connectionService;
        this.apiClient = apiClient;
    }

    @Transactional(readOnly = true)
    public SessionGitHubActivity activityForWindow(UUID userId, OffsetDateTime from, OffsetDateTime to,
            UUID preferredRepositoryId) {
        GitHubConnection connection = connectionRepository.findByUserId(userId).orElse(null);
        if (connection == null) {
            return new SessionGitHubActivity(false, List.of(), null, null);
        }

        String cacheKey = userId + "|" + preferredRepositoryId + "|" + from + "|" + to;
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.value();
        }

        String accessToken = connectionService.validAccessToken(userId);
        SessionGitHubActivity result = preferredRepositoryId != null
                ? activityForRepository(userId, accessToken, preferredRepositoryId, from, to)
                : activityAcrossRepositories(userId, accessToken, from, to);

        cache.put(cacheKey, new CacheEntry(result, Instant.now().plus(CACHE_TTL)));
        return result;
    }

    private SessionGitHubActivity activityForRepository(UUID userId, String accessToken, UUID repositoryId,
            OffsetDateTime from, OffsetDateTime to) {
        GitHubRepository repository = repositoryRepository.findById(repositoryId)
                .filter(r -> r.getUserId().equals(userId))
                .orElse(null);
        if (repository == null) {
            return new SessionGitHubActivity(true, List.of(), null, null);
        }
        List<GitHubCommitInfo> commits = fetchCommits(accessToken, repository, from, to);
        return new SessionGitHubActivity(true, commits, repository.getId(), repository.getFullName());
    }

    private SessionGitHubActivity activityAcrossRepositories(UUID userId, String accessToken, OffsetDateTime from,
            OffsetDateTime to) {
        List<GitHubRepository> repositories = repositoryRepository
                .findByUserIdOrderByRecentCommitCountDesc(userId).stream()
                .limit(MAX_REPOS_TO_SCAN)
                .toList();

        GitHubRepository bestMatch = null;
        List<GitHubCommitInfo> bestCommits = List.of();
        for (GitHubRepository repository : repositories) {
            List<GitHubCommitInfo> commits = fetchCommits(accessToken, repository, from, to);
            if (commits.size() > bestCommits.size()) {
                bestMatch = repository;
                bestCommits = commits;
            }
        }
        if (bestMatch == null) {
            return new SessionGitHubActivity(true, List.of(), null, null);
        }
        return new SessionGitHubActivity(true, bestCommits, bestMatch.getId(), bestMatch.getFullName());
    }

    @SuppressWarnings("unchecked")
    private List<GitHubCommitInfo> fetchCommits(String accessToken, GitHubRepository repository, OffsetDateTime from,
            OffsetDateTime to) {
        String url = "https://api.github.com/repos/%s/%s/commits?since=%s&until=%s&per_page=%d".formatted(
                repository.getOwner(), repository.getName(),
                encode(from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                encode(to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                MAX_COMMITS_PER_REPO);
        List<Map<String, Object>> raw;
        try {
            raw = apiClient.restGet(accessToken, url, new ParameterizedTypeReference<>() {});
        } catch (GitHubApiException e) {
            return List.of();
        }
        if (raw == null) {
            return List.of();
        }
        return raw.stream()
                .map(entry -> {
                    Map<String, Object> commit = (Map<String, Object>) entry.get("commit");
                    Map<String, Object> author = (Map<String, Object>) commit.get("author");
                    return new GitHubCommitInfo(
                            (String) entry.get("sha"),
                            (String) commit.get("message"),
                            OffsetDateTime.parse((String) author.get("date")),
                            repository.getFullName());
                })
                .toList();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
