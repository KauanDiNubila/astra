package com.astra.stats.dto;

import com.astra.learning.entity.GoalType;

public record GoalProgress(
        GoalType type,
        int targetHours,
        double achievedHours,
        boolean reached
) {
}
