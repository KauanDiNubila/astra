package com.astra.stats.adapter.out.learning;

import com.astra.learning.service.GoalService;
import com.astra.stats.application.port.out.LoadGoalsPort;
import com.astra.stats.domain.Goal;
import com.astra.stats.domain.GoalType;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GoalsAdapter implements LoadGoalsPort {

    private final GoalService goalService;

    GoalsAdapter(GoalService goalService) {
        this.goalService = goalService;
    }

    @Override
    public List<Goal> goalsOf(UUID userId) {
        return goalService.goalsForUser(userId).stream()
                .map(g -> new Goal(GoalType.valueOf(g.type().name()), g.targetHours()))
                .toList();
    }
}
