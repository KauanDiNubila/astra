package com.astra.github.application.port.in;

import com.astra.github.domain.LanguageShare;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record GitHubInsightsResponse(boolean connected, List<RepositoryInsight> repositories,
                                     List<LanguageShare> languages, List<DailyPoint> series,
                                     OffsetDateTime lastSyncedAt, String lastSyncError) {

    public record DailyPoint(LocalDate date, int contributionCount) {
    }
}
