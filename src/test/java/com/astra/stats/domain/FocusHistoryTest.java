package com.astra.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FocusHistoryTest {

    private static final LocalDate MONDAY = LocalDate.parse("2026-11-09");

    private static DayMinutes day(int daysAgo, long minutes) {
        return new DayMinutes(MONDAY.minusDays(daysAgo), minutes);
    }

    @Test
    void somaPorDiaEPorJanela() {
        FocusHistory history = new FocusHistory(List.of(day(0, 30), day(1, 20), day(7, 40), day(10, 5)));

        assertThat(history.minutesOn(MONDAY)).isEqualTo(30);
        assertThat(history.minutesOn(MONDAY.minusDays(2))).isZero();
        assertThat(history.minutesSince(MONDAY.minusDays(1))).isEqualTo(50);
        assertThat(history.minutesBetween(MONDAY.minusDays(7), MONDAY.minusDays(7))).isEqualTo(40);
        assertThat(history.minutesBetween(MONDAY.minusDays(10), MONDAY.minusDays(1))).isEqualTo(65);
    }

    @Test
    void sequenciaAtualContaDeHojeOuDeOntem() {
        assertThat(new FocusHistory(List.of(day(0, 1), day(1, 1), day(2, 1), day(4, 1))).currentStreak(MONDAY))
                .isEqualTo(3);
        assertThat(new FocusHistory(List.of(day(1, 1), day(2, 1))).currentStreak(MONDAY)).isEqualTo(2);
        assertThat(new FocusHistory(List.of(day(2, 1), day(3, 1))).currentStreak(MONDAY)).isZero();
        assertThat(new FocusHistory(List.of()).currentStreak(MONDAY)).isZero();
    }

    @Test
    void maiorSequenciaIgnoraDiasSemMinutos() {
        FocusHistory history = new FocusHistory(List.of(
                day(0, 10), day(5, 10), day(6, 0), day(7, 10), day(8, 10), day(9, 10)));

        assertThat(history.longestStreak()).isEqualTo(3);
        assertThat(new FocusHistory(List.of()).longestStreak()).isZero();
    }

    @Test
    void naoMudaSeAListaOriginalMudar() {
        List<DayMinutes> days = new ArrayList<>(List.of(day(0, 10)));
        FocusHistory history = new FocusHistory(days);
        days.add(day(1, 10));

        assertThat(history.days()).hasSize(1);
    }

    @Test
    void semanaComecaNaSegunda() {
        assertThat(FocusHistory.weekStartOf(MONDAY)).isEqualTo(MONDAY);
        assertThat(FocusHistory.weekStartOf(MONDAY.plusDays(6))).isEqualTo(MONDAY);
        assertThat(FocusHistory.weekStartOf(MONDAY.minusDays(1))).isEqualTo(MONDAY.minusDays(7));
    }
}
