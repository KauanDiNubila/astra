package com.astra.stats.application.port.in;

import com.astra.stats.domain.Goal;
import com.astra.stats.domain.GoalType;

public record GoalProgress(
        GoalType type,
        int targetHours,
        double achievedHours,
        boolean reached
) {

    public static GoalProgress of(Goal goal, long todayMinutes, long weekMinutes) {
        double achievedHours = goal.achievedHours(todayMinutes, weekMinutes);
        return new GoalProgress(goal.type(), goal.targetHours(), achievedHours, goal.isReachedWith(achievedHours));
    }
}
