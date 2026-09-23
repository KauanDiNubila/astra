package com.astra.learning.dto;

import com.astra.learning.entity.GoalType;

public record GoalResponse(
        GoalType type,
        int targetHours
) {
}
