package com.astra.stats.domain;

public record Goal(GoalType type, int targetHours) {

    public double achievedHours(long todayMinutes, long weekMinutes) {
        long minutes = type == GoalType.DAILY ? todayMinutes : weekMinutes;
        return minutes / 60.0;
    }

    public boolean isReachedWith(double achievedHours) {
        return achievedHours >= targetHours;
    }
}
