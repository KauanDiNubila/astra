package com.astra.stats.dto;

public record DashboardResponse(
        long todayMinutes,
        long weekMinutes,
        long totalMinutes,
        int currentStreak
) {
}
