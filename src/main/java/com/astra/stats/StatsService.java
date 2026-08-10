package com.astra.stats;

import com.astra.shared.CurrentUserProvider;
import com.astra.stats.dto.DashboardResponse;
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
    private final CurrentUserProvider currentUserProvider;

    public StatsService(SessionStatsService sessionStatsService, CurrentUserProvider currentUserProvider) {
        this.sessionStatsService = sessionStatsService;
        this.currentUserProvider = currentUserProvider;
    }

    public DashboardResponse dashboard() {
        UUID userId = currentUserProvider.currentUserId();
        LocalDate today = LocalDate.now(ZONE);
        OffsetDateTime startOfToday = today.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime streakWindow = today.minusDays(HEATMAP_DAYS + 1L).atStartOfDay(ZONE).toOffsetDateTime();

        long todayMinutes = sessionStatsService.focusedMinutesSince(userId, startOfToday);
        long weekMinutes = sessionStatsService.focusedMinutesSince(userId, startOfWeek);
        long totalMinutes = sessionStatsService.totalFocusedMinutes(userId);
        int streak = currentStreak(sessionStatsService.dailyMinutesSince(userId, streakWindow), today);

        return new DashboardResponse(todayMinutes, weekMinutes, totalMinutes, streak);
    }

    public List<DailyMinutes> heatmap() {
        UUID userId = currentUserProvider.currentUserId();
        LocalDate today = LocalDate.now(ZONE);
        OffsetDateTime start = today.minusDays(HEATMAP_DAYS).atStartOfDay(ZONE).toOffsetDateTime();
        return sessionStatsService.dailyMinutesSince(userId, start);
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
