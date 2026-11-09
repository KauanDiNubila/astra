package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.SetGoalCommand;
import com.astra.learning.domain.GoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

record SetGoalRequest(
        @NotNull GoalType type,
        @NotNull @Positive Integer targetHours
) {

    SetGoalCommand toCommand(UUID userId) {
        return new SetGoalCommand(userId, type, targetHours);
    }
}
