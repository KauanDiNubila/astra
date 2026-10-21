package com.astra.stats.dto;

import java.util.List;

public record DashboardResponse(
        long todayMinutes,
        long weekMinutes,
        long totalMinutes,
        int currentStreak,
        long previousWeekMinutes,
        long last30DaysMinutes,
        int longestStreak,
        List<GoalProgress> goals
) {
}
