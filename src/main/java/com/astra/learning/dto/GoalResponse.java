package com.astra.learning.dto;

import com.astra.learning.GoalType;

public record GoalResponse(
        GoalType type,
        int targetHours
) {
}
