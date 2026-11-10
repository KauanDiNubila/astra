package com.astra.github.application.port.out;

import com.astra.github.domain.DailyContribution;
import com.astra.github.domain.PeriodSummary;
import com.astra.github.domain.SyncedRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadSyncedDataPort {

    List<SyncedRepository> repositoriesByActivity(UUID userId);

    Optional<SyncedRepository> repository(UUID repositoryId);

    List<SyncedRepository> repositories(Collection<UUID> repositoryIds);

    List<DailyContribution> dailySince(UUID userId, LocalDate since);

    List<DailyContribution> allDaily(UUID userId);

    List<PeriodSummary> summaries(UUID userId);
}
