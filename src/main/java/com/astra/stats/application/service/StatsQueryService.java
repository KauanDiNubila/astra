package com.astra.stats.application.service;

import com.astra.stats.application.port.in.DashboardResponse;
import com.astra.stats.application.port.in.GetCategoryBreakdownQuery;
import com.astra.stats.application.port.in.GetDashboardQuery;
import com.astra.stats.application.port.in.GetHeatmapQuery;
import com.astra.stats.application.port.in.GoalProgress;
import com.astra.stats.application.port.out.LoadFocusedMinutesPort;
import com.astra.stats.application.port.out.LoadGoalsPort;
import com.astra.stats.domain.CategoryMinutes;
import com.astra.stats.domain.DayMinutes;
import com.astra.stats.domain.FocusHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class StatsQueryService implements GetDashboardQuery, GetHeatmapQuery, GetCategoryBreakdownQuery {

    static final int HEATMAP_DAYS = 365;

    private final LoadFocusedMinutesPort loadFocusedMinutesPort;
    private final LoadGoalsPort loadGoalsPort;

    StatsQueryService(LoadFocusedMinutesPort loadFocusedMinutesPort, LoadGoalsPort loadGoalsPort) {
        this.loadFocusedMinutesPort = loadFocusedMinutesPort;
        this.loadGoalsPort = loadGoalsPort;
    }

    @Override
    public DashboardResponse dashboard(UUID userId) {
        LocalDate today = StatsCalendar.today();
        LocalDate weekStart = FocusHistory.weekStartOf(today);
        FocusHistory history = new FocusHistory(loadFocusedMinutesPort.dailySince(
                userId, StatsCalendar.startOf(today.minusDays(HEATMAP_DAYS + 1L))));

        long todayMinutes = history.minutesOn(today);
        long weekMinutes = history.minutesSince(weekStart);
        List<GoalProgress> goals = loadGoalsPort.goalsOf(userId).stream()
                .map(goal -> GoalProgress.of(goal, todayMinutes, weekMinutes))
                .toList();

        return new DashboardResponse(
                todayMinutes,
                weekMinutes,
                loadFocusedMinutesPort.totalMinutes(userId),
                history.currentStreak(today),
                history.minutesBetween(weekStart.minusWeeks(1), today.minusWeeks(1)),
                history.minutesSince(today.minusDays(29)),
                history.longestStreak(),
                goals);
    }

    @Override
    public List<DayMinutes> heatmap(UUID userId) {
        return loadFocusedMinutesPort.dailySince(
                userId, StatsCalendar.startOf(StatsCalendar.today().minusDays(HEATMAP_DAYS)));
    }

    @Override
    public List<CategoryMinutes> breakdown(UUID userId, int days) {
        return loadFocusedMinutesPort.byCategorySince(
                userId, StatsCalendar.startOf(StatsCalendar.today().minusDays(days - 1L)));
    }
}
