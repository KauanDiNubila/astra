package com.astra.learning.dto;

import com.astra.learning.GoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SetGoalRequest(
        @NotNull GoalType type,
        @NotNull @Positive Integer targetHours
) {
}
