package com.astra.learning.domain;

import java.util.Objects;
import java.util.UUID;

public record Goal(UUID userId, GoalType type, int targetHours) {

    public Goal {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(type);
        if (targetHours <= 0) {
            throw new IllegalArgumentException("targetHours must be positive");
        }
    }
}
