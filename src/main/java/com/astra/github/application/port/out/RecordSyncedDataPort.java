package com.astra.github.application.port.out;

import com.astra.github.domain.DailyContribution;
import com.astra.github.domain.PeriodSummary;
import com.astra.github.domain.RepositorySnapshot;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface RecordSyncedDataPort {

    void recordDaily(UUID userId, List<DailyContribution> days);

    void recordRepositories(UUID userId, List<RepositorySnapshot> repositories, OffsetDateTime syncedAt);

    void recordSummary(UUID userId, PeriodSummary summary);
}
