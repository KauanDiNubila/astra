package com.astra.github.adapter.out.api;

import com.astra.github.application.port.out.LoadContributionsPort;
import com.astra.github.domain.ActivityPeriod;
import com.astra.github.domain.DailyContribution;
import com.astra.github.domain.PeriodSummary;
import com.astra.github.domain.RepositorySnapshot;
import com.astra.github.domain.YearSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
class GitHubContributionsAdapter implements LoadContributionsPort {

    private static final DateTimeFormatter SEARCH_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final String YEAR_QUERY = """
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

    private final GitHubApiClient apiClient;
    private final Clock clock;

    GitHubContributionsAdapter(GitHubApiClient apiClient, Clock clock) {
        this.apiClient = apiClient;
        this.clock = clock;
    }

    @Override
    public YearSnapshot year(String accessToken, String login, LocalDate from, LocalDate to) {
        JsonNode data = apiClient.graphql(accessToken, YEAR_QUERY, variables(login, from, to));
        JsonNode collection = data.at("/viewer/contributionsCollection");
        return new YearSnapshot(daily(collection.at("/contributionCalendar/weeks")),
                repositories(collection.at("/commitContributionsByRepository")),
                summary(ActivityPeriod.YEAR, collection, data));
    }

    @Override
    public PeriodSummary period(String accessToken, String login, ActivityPeriod period, LocalDate from,
                                LocalDate to) {
        JsonNode data = apiClient.graphql(accessToken, PERIOD_QUERY, variables(login, from, to));
        return summary(period, data.at("/viewer/contributionsCollection"), data);
    }

    private Map<String, Object> variables(String login, LocalDate from, LocalDate to) {
        String fromIso = from.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String toIso = clock.instant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String mergedQuery = "author:%s is:pr is:merged merged:%s..%s".formatted(
                login, SEARCH_DATE.format(from), SEARCH_DATE.format(to));
        String closedQuery = "author:%s is:issue is:closed closed:%s..%s".formatted(
                login, SEARCH_DATE.format(from), SEARCH_DATE.format(to));
        return Map.of("from", fromIso, "to", toIso, "mergedQuery", mergedQuery, "closedQuery", closedQuery);
    }

    private static List<DailyContribution> daily(JsonNode weeks) {
        List<DailyContribution> days = new ArrayList<>();
        for (JsonNode week : weeks) {
            for (JsonNode day : week.path("contributionDays")) {
                days.add(new DailyContribution(LocalDate.parse(day.get("date").asText()),
                        day.get("contributionCount").asInt()));
            }
        }
        return days;
    }

    private static List<RepositorySnapshot> repositories(JsonNode entries) {
        List<RepositorySnapshot> repositories = new ArrayList<>();
        for (JsonNode entry : entries) {
            JsonNode repo = entry.get("repository");
            repositories.add(new RepositorySnapshot(repo.get("databaseId").asLong(),
                    repo.at("/owner/login").asText(), repo.get("name").asText(), repo.get("nameWithOwner").asText(),
                    textOrNull(repo.get("description")), textOrNull(repo.at("/primaryLanguage/name")),
                    repo.get("url").asText(), repo.get("isPrivate").asBoolean(), repo.get("stargazerCount").asInt(),
                    repo.get("forkCount").asInt(), textOrNull(repo.at("/defaultBranchRef/name")),
                    dateTimeOrNull(repo.get("pushedAt")), dateTimeOrNull(repo.get("createdAt")),
                    entry.at("/contributions/totalCount").asInt()));
        }
        return repositories;
    }

    private static PeriodSummary summary(ActivityPeriod period, JsonNode collection, JsonNode root) {
        return new PeriodSummary(period, collection.path("totalCommitContributions").asInt(),
                collection.path("totalPullRequestContributions").asInt(), root.at("/mergedPRs/issueCount").asInt(),
                root.at("/closedIssues/issueCount").asInt(), collection.path("commitContributionsByRepository").size());
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode() ? null : node.asText();
    }

    private static OffsetDateTime dateTimeOrNull(JsonNode node) {
        String text = textOrNull(node);
        return text == null ? null : OffsetDateTime.parse(text);
    }
}
