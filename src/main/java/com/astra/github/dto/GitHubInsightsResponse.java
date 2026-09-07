package com.astra.github.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record GitHubInsightsResponse(
        boolean connected,
        List<GitHubRepositoryInsight> repositories,
        List<GitHubLanguageShare> languages,
        List<GitHubDailyPoint> series,
        OffsetDateTime lastSyncedAt,
        String lastSyncError) {
}
