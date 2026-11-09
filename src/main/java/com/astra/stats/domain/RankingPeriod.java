package com.astra.stats.domain;

import java.time.LocalDate;

public enum RankingPeriod {
    DAILY,
    WEEKLY,
    MONTHLY;

    public LocalDate startOn(LocalDate today) {
        return switch (this) {
            case DAILY -> today;
            case WEEKLY -> FocusHistory.weekStartOf(today);
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }
}
