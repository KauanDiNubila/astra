package com.astra.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RankingPeriodTest {

    private static final LocalDate THURSDAY = LocalDate.parse("2026-11-12");

    @Test
    void cadaPeriodoComecaNoSeuPrimeiroDia() {
        assertThat(RankingPeriod.DAILY.startOn(THURSDAY)).isEqualTo(THURSDAY);
        assertThat(RankingPeriod.WEEKLY.startOn(THURSDAY)).isEqualTo(LocalDate.parse("2026-11-09"));
        assertThat(RankingPeriod.MONTHLY.startOn(THURSDAY)).isEqualTo(LocalDate.parse("2026-11-01"));
    }
}
