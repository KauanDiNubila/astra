package com.astra.stats.adapter.out.learning;

import com.astra.learning.application.port.in.ListGoalsQuery;
import com.astra.stats.application.port.out.LoadGoalsPort;
import com.astra.stats.domain.Goal;
import com.astra.stats.domain.GoalType;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GoalsAdapter implements LoadGoalsPort {

    private final ListGoalsQuery listGoalsQuery;

    GoalsAdapter(ListGoalsQuery listGoalsQuery) {
        this.listGoalsQuery = listGoalsQuery;
    }

    @Override
    public List<Goal> goalsOf(UUID userId) {
        return listGoalsQuery.list(userId).stream()
                .map(g -> new Goal(GoalType.valueOf(g.type()), g.targetHours()))
                .toList();
    }
}
