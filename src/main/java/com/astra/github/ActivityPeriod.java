package com.astra.github;

import java.time.LocalDate;

public enum ActivityPeriod {
    TODAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR;

    public LocalDate since(LocalDate today) {
        return switch (this) {
            case TODAY -> today;
            case WEEK -> today.minusDays(7);
            case MONTH -> today.minusDays(30);
            case QUARTER -> today.minusDays(90);
            case YEAR -> today.minusDays(365);
        };
    }
}
