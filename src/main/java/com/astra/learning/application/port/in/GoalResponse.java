package com.astra.learning.application.port.in;

import com.astra.learning.domain.Goal;

public record GoalResponse(String type, int targetHours) {

    public static GoalResponse from(Goal goal) {
        return new GoalResponse(goal.type().name(), goal.targetHours());
    }
}
