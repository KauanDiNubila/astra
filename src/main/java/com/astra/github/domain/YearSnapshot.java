package com.astra.github.domain;

import java.util.List;

public record YearSnapshot(List<DailyContribution> daily, List<RepositorySnapshot> repositories,
                           PeriodSummary summary) {
}
