package com.astra.github.application.service;

import com.astra.github.application.port.in.CheckRepositoryOwnerQuery;
import com.astra.github.application.port.in.ExportGitHubQuery;
import com.astra.github.application.port.in.GetGitHubActivityQuery;
import com.astra.github.application.port.in.GetGitHubInsightsQuery;
import com.astra.github.application.port.in.GetGitHubStatusQuery;
import com.astra.github.application.port.in.GetStepEvidenceQuery;
import com.astra.github.application.port.in.GitHubActivityResponse;
import com.astra.github.application.port.in.GitHubConnectionStatus;
import com.astra.github.application.port.in.GitHubInsightsResponse;
import com.astra.github.application.port.in.GitHubInsightsResponse.DailyPoint;
import com.astra.github.application.port.in.ListRepositoriesQuery;
import com.astra.github.application.port.in.ListVisibleProfilesQuery;
import com.astra.github.application.port.in.RepositoryInsight;
import com.astra.github.application.port.in.StepEvidenceResponse;
import com.astra.github.application.port.in.VisibleProfile;
import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.application.port.out.LoadSyncedDataPort;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.LanguageShare;
import com.astra.github.domain.PeriodSummary;
import com.astra.github.domain.StepEvidence;
import com.astra.github.domain.SyncedRepository;
import com.astra.shared.ExportRow;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GitHubQueryService implements GetGitHubStatusQuery, GetGitHubActivityQuery, GetGitHubInsightsQuery,
        GetStepEvidenceQuery, ListVisibleProfilesQuery, CheckRepositoryOwnerQuery, ListRepositoriesQuery,
        ExportGitHubQuery {

    private static final int SERIES_DAYS = 364;

    private final LoadConnectionPort loadConnectionPort;
    private final LoadSyncedDataPort loadSyncedDataPort;
    private final Clock clock;

    GitHubQueryService(LoadConnectionPort loadConnectionPort, LoadSyncedDataPort loadSyncedDataPort, Clock clock) {
        this.loadConnectionPort = loadConnectionPort;
        this.loadSyncedDataPort = loadSyncedDataPort;
        this.clock = clock;
    }

    @Override
    public GitHubConnectionStatus status(UUID userId) {
        return loadConnectionPort.loadByUser(userId)
                .map(c -> new GitHubConnectionStatus(true, c.getProfile().login(), c.getProfile().avatarUrl(),
                        c.getConnectedAt(), c.getLastSyncedAt(), c.getLastSyncError(), c.isVisibleToFriends()))
                .orElseGet(GitHubConnectionStatus::disconnected);
    }

    @Override
    public GitHubActivityResponse activity(UUID userId) {
        Optional<GitHubConnection> connection = loadConnectionPort.loadByUser(userId);
        if (connection.isEmpty()) {
            return new GitHubActivityResponse(false, List.of(), null, null);
        }
        List<PeriodSummary> periods = loadSyncedDataPort.summaries(userId).stream()
                .sorted(Comparator.comparing(PeriodSummary::period))
                .toList();
        return new GitHubActivityResponse(true, periods, connection.get().getLastSyncedAt(),
                connection.get().getLastSyncError());
    }

    @Override
    public GitHubInsightsResponse insights(UUID userId) {
        Optional<GitHubConnection> connection = loadConnectionPort.loadByUser(userId);
        if (connection.isEmpty()) {
            return new GitHubInsightsResponse(false, List.of(), List.of(), List.of(), null, null);
        }
        List<SyncedRepository> repositories = loadSyncedDataPort.repositoriesByActivity(userId);
        LocalDate since = LocalDate.now(clock.withZone(ZoneOffset.UTC)).minusDays(SERIES_DAYS);
        List<DailyPoint> series = loadSyncedDataPort.dailySince(userId, since).stream()
                .map(d -> new DailyPoint(d.date(), d.count()))
                .toList();
        return new GitHubInsightsResponse(true, repositories.stream().map(RepositoryInsight::from).toList(),
                LanguageShare.of(repositories), series, connection.get().getLastSyncedAt(),
                connection.get().getLastSyncError());
    }

    @Override
    public StepEvidenceResponse evidenceFor(UUID userId, String stepTitle) {
        if (loadConnectionPort.loadByUser(userId).isEmpty()) {
            return new StepEvidenceResponse(false, List.of());
        }
        return new StepEvidenceResponse(true,
                StepEvidence.matching(stepTitle, loadSyncedDataPort.repositoriesByActivity(userId)).stream()
                        .map(RepositoryInsight::from)
                        .toList());
    }

    @Override
    public Map<UUID, VisibleProfile> visibleToFriends(Collection<UUID> userIds) {
        return loadConnectionPort.loadByUsers(userIds).stream()
                .filter(GitHubConnection::isVisibleToFriends)
                .collect(Collectors.toMap(GitHubConnection::getUserId,
                        c -> new VisibleProfile(c.getProfile().login(), c.getProfile().avatarUrl())));
    }

    @Override
    public boolean isOwner(UUID repositoryId, UUID userId) {
        return loadSyncedDataPort.repository(repositoryId).filter(r -> r.belongsTo(userId)).isPresent();
    }

    @Override
    public List<RepositoryInsight> byIds(Collection<UUID> repositoryIds) {
        return loadSyncedDataPort.repositories(repositoryIds).stream().map(RepositoryInsight::from).toList();
    }

    @Override
    public Map<String, Object> exportGitHub(UUID userId) {
        return loadConnectionPort.loadByUser(userId)
                .map(connection -> ExportRow.of(
                        "login", connection.getProfile().login(),
                        "avatarUrl", connection.getProfile().avatarUrl(),
                        "connectedAt", connection.getConnectedAt(),
                        "lastSyncedAt", connection.getLastSyncedAt(),
                        "visibleToFriends", connection.isVisibleToFriends(),
                        "activitySummaries", loadSyncedDataPort.summaries(userId).stream()
                                .map(s -> ExportRow.of("period", s.period(), "commits", s.commitCount(),
                                        "pullRequestsOpened", s.pullRequestOpenedCount(),
                                        "pullRequestsMerged", s.pullRequestMergedCount(),
                                        "issuesClosed", s.issueClosedCount(),
                                        "activeRepositories", s.activeRepoCount()))
                                .toList(),
                        "dailyContributions", loadSyncedDataPort.allDaily(userId).stream()
                                .map(d -> ExportRow.of("date", d.date(), "contributions", d.count()))
                                .toList(),
                        "repositories", loadSyncedDataPort.repositoriesByActivity(userId).stream()
                                .map(r -> ExportRow.of("id", r.id(), "fullName", r.fullName(),
                                        "description", r.description(), "language", r.primaryLanguage(),
                                        "url", r.htmlUrl(), "private", r.isPrivate(), "stars", r.starCount(),
                                        "forks", r.forkCount(), "pushedAt", r.pushedAt(),
                                        "recentCommits", r.recentCommitCount()))
                                .toList()))
                .orElse(null);
    }
}
