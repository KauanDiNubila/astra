package com.astra.stats.application.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

final class StatsCalendar {

    static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private StatsCalendar() {
    }

    static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    static OffsetDateTime startOf(LocalDate day) {
        return day.atStartOfDay(ZONE).toOffsetDateTime();
    }
}
