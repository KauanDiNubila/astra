package com.astra.stats.dto;

import com.astra.learning.GoalType;

public record GoalProgress(
        GoalType type,
        int targetHours,
        double achievedHours,
        boolean reached
) {
}
