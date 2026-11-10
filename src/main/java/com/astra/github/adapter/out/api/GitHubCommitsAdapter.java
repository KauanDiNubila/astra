package com.astra.github.adapter.out.api;

import com.astra.github.application.port.out.LoadCommitsPort;
import com.astra.github.domain.CommitInfo;
import com.astra.github.domain.GitHubApiException;
import com.astra.github.domain.SyncedRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
class GitHubCommitsAdapter implements LoadCommitsPort {

    private static final int MAX_COMMITS_PER_REPO = 30;

    private final GitHubApiClient apiClient;

    GitHubCommitsAdapter(GitHubApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CommitInfo> commits(String accessToken, SyncedRepository repository, OffsetDateTime from,
                                    OffsetDateTime to) {
        String url = "https://api.github.com/repos/%s/%s/commits?since=%s&until=%s&per_page=%d".formatted(
                repository.owner(), repository.name(), encode(from), encode(to), MAX_COMMITS_PER_REPO);
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
                    return new CommitInfo((String) entry.get("sha"), (String) commit.get("message"),
                            OffsetDateTime.parse((String) author.get("date")), repository.fullName());
                })
                .toList();
    }

    private static String encode(OffsetDateTime value) {
        return URLEncoder.encode(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), StandardCharsets.UTF_8);
    }
}
