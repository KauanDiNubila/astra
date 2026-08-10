package com.astra.stats;

import com.astra.shared.CurrentUserProvider;
import com.astra.stats.dto.DashboardResponse;
import com.astra.tracking.session.SessionStatsService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

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

        long todayMinutes = sessionStatsService.focusedMinutesSince(userId, startOfToday);
        long weekMinutes = sessionStatsService.focusedMinutesSince(userId, startOfWeek);
        long totalMinutes = sessionStatsService.totalFocusedMinutes(userId);
        return new DashboardResponse(todayMinutes, weekMinutes, totalMinutes);
    }
}
