package com.astra.stats.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record FocusHistory(List<DayMinutes> days) {

    public FocusHistory {
        days = List.copyOf(days);
    }

    public static LocalDate weekStartOf(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public long minutesOn(LocalDate day) {
        return days.stream()
                .filter(d -> d.day().equals(day))
                .mapToLong(DayMinutes::minutes)
                .findFirst()
                .orElse(0);
    }

    public long minutesSince(LocalDate start) {
        return days.stream()
                .filter(d -> !d.day().isBefore(start))
                .mapToLong(DayMinutes::minutes)
                .sum();
    }

    public long minutesBetween(LocalDate start, LocalDate end) {
        return days.stream()
                .filter(d -> !d.day().isBefore(start) && !d.day().isAfter(end))
                .mapToLong(DayMinutes::minutes)
                .sum();
    }

    public int currentStreak(LocalDate today) {
        Set<LocalDate> active = days.stream().map(DayMinutes::day).collect(Collectors.toSet());
        LocalDate cursor;
        if (active.contains(today)) {
            cursor = today;
        } else if (active.contains(today.minusDays(1))) {
            cursor = today.minusDays(1);
        } else {
            return 0;
        }
        int streak = 0;
        while (active.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public int longestStreak() {
        List<LocalDate> active = days.stream()
                .filter(d -> d.minutes() > 0)
                .map(DayMinutes::day)
                .distinct()
                .sorted()
                .toList();
        int best = 0;
        int current = 0;
        LocalDate previous = null;
        for (LocalDate day : active) {
            current = previous != null && previous.plusDays(1).equals(day) ? current + 1 : 1;
            best = Math.max(best, current);
            previous = day;
        }
        return best;
    }
}
