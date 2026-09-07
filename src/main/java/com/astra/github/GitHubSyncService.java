package com.astra.github;

import com.astra.shared.exception.ConflictException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GitHubSyncService {

    private static final DateTimeFormatter SEARCH_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Duration SYNC_COOLDOWN = Duration.ofMinutes(2);

    private static final String FULL_QUERY = """
            query($from: DateTime!, $to: DateTime!, $mergedQuery: String!, $closedQuery: String!) {
              viewer {
                login
                contributionsCollection(from: $from, to: $to) {
                  totalCommitContributions
                  totalPullRequestContributions
                  contributionCalendar {
                    weeks { contributionDays { date contributionCount } }
                  }
                  commitContributionsByRepository(maxRepositories: 25) {
                    repository {
                      databaseId
                      name
                      nameWithOwner
                      owner { login }
                      description
                      primaryLanguage { name }
                      url
                      isPrivate
                      stargazerCount
                      forkCount
                      defaultBranchRef { name }
                      pushedAt
                      createdAt
                    }
                    contributions { totalCount }
                  }
                }
              }
              mergedPRs: search(query: $mergedQuery, type: ISSUE, first: 0) { issueCount }
              closedIssues: search(query: $closedQuery, type: ISSUE, first: 0) { issueCount }
            }
            """;

    private static final String PERIOD_QUERY = """
            query($from: DateTime!, $to: DateTime!, $mergedQuery: String!, $closedQuery: String!) {
              viewer {
                contributionsCollection(from: $from, to: $to) {
                  totalCommitContributions
                  totalPullRequestContributions
                  commitContributionsByRepository(maxRepositories: 25) {
                    repository { nameWithOwner }
                  }
                }
              }
              mergedPRs: search(query: $mergedQuery, type: ISSUE, first: 0) { issueCount }
              closedIssues: search(query: $closedQuery, type: ISSUE, first: 0) { issueCount }
            }
            """;

    private final GitHubConnectionService connectionService;
    private final GitHubApiClient apiClient;
    private final GitHubRepositoryRepository repositoryRepository;
    private final GitHubDailyActivityRepository dailyActivityRepository;
    private final GitHubActivitySummaryRepository activitySummaryRepository;

    public GitHubSyncService(GitHubConnectionService connectionService, GitHubApiClient apiClient,
            GitHubRepositoryRepository repositoryRepository,
            GitHubDailyActivityRepository dailyActivityRepository,
            GitHubActivitySummaryRepository activitySummaryRepository) {
        this.connectionService = connectionService;
        this.apiClient = apiClient;
        this.repositoryRepository = repositoryRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.activitySummaryRepository = activitySummaryRepository;
    }

    @Transactional
    public void sync(UUID userId) {
        GitHubConnection connection = connectionService.requireConnection(userId);
        if (connection.getLastSyncedAt() != null
                && connection.getLastSyncedAt().isAfter(OffsetDateTime.now().minus(SYNC_COOLDOWN))) {
            throw new ConflictException("Aguarde alguns minutos antes de sincronizar de novo");
        }
        String login = connection.getGithubLogin();
        String accessToken = connectionService.validAccessToken(userId);

        try {
            syncYear(userId, login, accessToken);
            syncPeriod(userId, login, accessToken, ActivityPeriod.WEEK);
            syncPeriod(userId, login, accessToken, ActivityPeriod.MONTH);
            syncPeriod(userId, login, accessToken, ActivityPeriod.QUARTER);
            syncPeriod(userId, login, accessToken, ActivityPeriod.TODAY);
            connectionService.markSyncResult(userId, OffsetDateTime.now(), null);
        } catch (GitHubApiException e) {
            connectionService.markSyncResult(userId, OffsetDateTime.now(), e.getMessage());
            throw e;
        }
    }

    private void syncYear(UUID userId, String login, String accessToken) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = today.minusDays(365);
        JsonNode data = apiClient.graphql(accessToken, FULL_QUERY, variables(login, from, today));

        JsonNode collection = data.at("/viewer/contributionsCollection");
        upsertDailyActivity(userId, collection.at("/contributionCalendar/weeks"));
        upsertRepositories(userId, collection.at("/commitContributionsByRepository"));
        saveSummary(userId, ActivityPeriod.YEAR, collection, data);
    }

    private void syncPeriod(UUID userId, String login, String accessToken, ActivityPeriod period) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = period.since(today);
        JsonNode data = apiClient.graphql(accessToken, PERIOD_QUERY, variables(login, from, today));
        JsonNode collection = data.at("/viewer/contributionsCollection");
        saveSummary(userId, period, collection, data);
    }

    private Map<String, Object> variables(String login, LocalDate from, LocalDate to) {
        String fromIso = from.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String toIso = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String mergedQuery = "author:%s is:pr is:merged merged:%s..%s".formatted(
                login, SEARCH_DATE.format(from), SEARCH_DATE.format(to));
        String closedQuery = "author:%s is:issue is:closed closed:%s..%s".formatted(
                login, SEARCH_DATE.format(from), SEARCH_DATE.format(to));
        return Map.of("from", fromIso, "to", toIso, "mergedQuery", mergedQuery, "closedQuery", closedQuery);
    }

    private void upsertDailyActivity(UUID userId, JsonNode weeks) {
        if (!weeks.isArray()) {
            return;
        }
        for (JsonNode week : weeks) {
            for (JsonNode day : week.path("contributionDays")) {
                LocalDate date = LocalDate.parse(day.get("date").asText());
                int count = day.get("contributionCount").asInt();
                GitHubDailyActivity activity = dailyActivityRepository.findByUserIdAndActivityDate(userId, date)
                        .orElseGet(() -> new GitHubDailyActivity(userId, date, count));
                activity.setContributionCount(count);
                dailyActivityRepository.save(activity);
            }
        }
    }

    private void upsertRepositories(UUID userId, JsonNode commitContributionsByRepository) {
        if (!commitContributionsByRepository.isArray()) {
            return;
        }
        for (JsonNode entry : commitContributionsByRepository) {
            JsonNode repo = entry.get("repository");
            long githubRepoId = repo.get("databaseId").asLong();
            int recentCommitCount = entry.at("/contributions/totalCount").asInt();
            JsonNode language = repo.get("primaryLanguage");
            JsonNode defaultBranchRef = repo.get("defaultBranchRef");

            GitHubRepository record = repositoryRepository.findByUserIdAndGithubRepoId(userId, githubRepoId)
                    .orElseGet(() -> new GitHubRepository(userId, githubRepoId,
                            repo.at("/owner/login").asText(),
                            repo.get("name").asText(),
                            repo.get("nameWithOwner").asText(),
                            textOrNull(repo.get("description")),
                            language == null || language.isNull() ? null : language.get("name").asText(),
                            repo.get("url").asText(),
                            repo.get("isPrivate").asBoolean(),
                            repo.get("stargazerCount").asInt(),
                            repo.get("forkCount").asInt(),
                            defaultBranchRef == null || defaultBranchRef.isNull()
                                    ? null : defaultBranchRef.get("name").asText(),
                            parseInstant(repo.get("pushedAt")),
                            parseInstant(repo.get("createdAt"))));
            record.setDescription(textOrNull(repo.get("description")));
            record.setPrimaryLanguage(language == null || language.isNull() ? null : language.get("name").asText());
            record.setStarCount(repo.get("stargazerCount").asInt());
            record.setForkCount(repo.get("forkCount").asInt());
            record.setPushedAt(parseInstant(repo.get("pushedAt")));
            record.setRecentCommitCount(recentCommitCount);
            record.setSyncedAt(OffsetDateTime.now());
            repositoryRepository.save(record);
        }
    }

    private void saveSummary(UUID userId, ActivityPeriod period, JsonNode collection, JsonNode root) {
        int commitCount = collection.path("totalCommitContributions").asInt();
        int prOpened = collection.path("totalPullRequestContributions").asInt();
        int prMerged = root.at("/mergedPRs/issueCount").asInt();
        int issuesClosed = root.at("/closedIssues/issueCount").asInt();
        int activeRepos = collection.path("commitContributionsByRepository").size();

        GitHubActivitySummary summary = activitySummaryRepository.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> new GitHubActivitySummary(userId, period, commitCount, prOpened, prMerged,
                        issuesClosed, activeRepos));
        summary.setCommitCount(commitCount);
        summary.setPullRequestOpenedCount(prOpened);
        summary.setPullRequestMergedCount(prMerged);
        summary.setIssueClosedCount(issuesClosed);
        summary.setActiveRepoCount(activeRepos);
        activitySummaryRepository.save(summary);
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static OffsetDateTime parseInstant(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return OffsetDateTime.parse(node.asText());
    }
}
