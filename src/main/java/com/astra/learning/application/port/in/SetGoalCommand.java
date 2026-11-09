package com.astra.learning.application.port.in;

import com.astra.learning.domain.GoalType;
import java.util.Objects;
import java.util.UUID;

public record SetGoalCommand(UUID userId, GoalType type, int targetHours) {

    public SetGoalCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(type);
        if (targetHours <= 0) {
            throw new IllegalArgumentException("targetHours must be positive");
        }
    }
}
