package com.astra.github.application.port.in;

import com.astra.github.domain.PeriodSummary;
import java.time.OffsetDateTime;
import java.util.List;

public record GitHubActivityResponse(boolean connected, List<PeriodSummary> periods, OffsetDateTime lastSyncedAt,
                                     String lastSyncError) {
}
