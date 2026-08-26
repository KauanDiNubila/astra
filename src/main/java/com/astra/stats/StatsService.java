package com.astra.stats;

import com.astra.learning.GoalService;
import com.astra.learning.GoalType;
import com.astra.shared.CurrentUserProvider;
import com.astra.stats.dto.DashboardResponse;
import com.astra.stats.dto.GoalProgress;
import com.astra.tracking.session.CategoryMinutes;
import com.astra.tracking.session.DailyMinutes;
import com.astra.tracking.session.SessionStatsService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final int HEATMAP_DAYS = 365;

    private final SessionStatsService sessionStatsService;
    private final GoalService goalService;
    private final CurrentUserProvider currentUserProvider;

    public StatsService(SessionStatsService sessionStatsService, GoalService goalService,
                        CurrentUserProvider currentUserProvider) {
        this.sessionStatsService = sessionStatsService;
        this.goalService = goalService;
        this.currentUserProvider = currentUserProvider;
    }

    public DashboardResponse dashboard() {
        UUID userId = currentUserProvider.currentUserId();
        LocalDate today = LocalDate.now(ZONE);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        OffsetDateTime streakWindow = today.minusDays(HEATMAP_DAYS + 1L).atStartOfDay(ZONE).toOffsetDateTime();

        // hoje/semana/streak vêm todos do mesmo intervalo de 366 dias — uma query só,
        // em vez de uma SUM separada pra cada, economiza viagens ao banco
        List<DailyMinutes> dailyMinutes = sessionStatsService.dailyMinutesSince(userId, streakWindow);
        long todayMinutes = minutesForDay(dailyMinutes, today);
        long weekMinutes = sumMinutesSince(dailyMinutes, weekStart);
        long totalMinutes = sessionStatsService.totalFocusedMinutes(userId);
        int streak = currentStreak(dailyMinutes, today);
        List<GoalProgress> goals = goalProgress(userId, todayMinutes, weekMinutes);

        return new DashboardResponse(todayMinutes, weekMinutes, totalMinutes, streak, goals);
    }

    private long minutesForDay(List<DailyMinutes> days, LocalDate day) {
        return days.stream()
                .filter(d -> d.day().equals(day))
                .mapToLong(DailyMinutes::minutes)
                .findFirst()
                .orElse(0);
    }

    private long sumMinutesSince(List<DailyMinutes> days, LocalDate start) {
        return days.stream()
                .filter(d -> !d.day().isBefore(start))
                .mapToLong(DailyMinutes::minutes)
                .sum();
    }

    public List<DailyMinutes> heatmap() {
        UUID userId = currentUserProvider.currentUserId();
        LocalDate today = LocalDate.now(ZONE);
        OffsetDateTime start = today.minusDays(HEATMAP_DAYS).atStartOfDay(ZONE).toOffsetDateTime();
        return sessionStatsService.dailyMinutesSince(userId, start);
    }

    public List<CategoryMinutes> categoryBreakdown(int days) {
        UUID userId = currentUserProvider.currentUserId();
        LocalDate today = LocalDate.now(ZONE);
        OffsetDateTime start = today.minusDays(days - 1L).atStartOfDay(ZONE).toOffsetDateTime();
        return sessionStatsService.categoryMinutesSince(userId, start);
    }

    private List<GoalProgress> goalProgress(UUID userId, long todayMinutes, long weekMinutes) {
        return goalService.goalsForUser(userId).stream()
                .map(g -> {
                    long achievedMinutes = g.type() == GoalType.DAILY ? todayMinutes : weekMinutes;
                    double achievedHours = achievedMinutes / 60.0;
                    boolean reached = achievedHours >= g.targetHours();
                    return new GoalProgress(g.type(), g.targetHours(), achievedHours, reached);
                })
                .toList();
    }

    private int currentStreak(List<DailyMinutes> days, LocalDate today) {
        Set<LocalDate> active = days.stream().map(DailyMinutes::day).collect(Collectors.toSet());
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
}
