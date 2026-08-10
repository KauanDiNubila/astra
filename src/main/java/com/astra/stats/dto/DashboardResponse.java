package com.astra.stats.dto;

import java.util.List;

public record DashboardResponse(
        long todayMinutes,
        long weekMinutes,
        long totalMinutes,
        int currentStreak,
        List<GoalProgress> goals
) {
}
