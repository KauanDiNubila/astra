package com.astra.github.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record GitHubActivityResponse(
        boolean connected,
        List<GitHubPeriodSummary> periods,
        OffsetDateTime lastSyncedAt,
        String lastSyncError) {
}
