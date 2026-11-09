package com.astra.learning.adapter.out.persistence;

import com.astra.learning.application.port.out.LoadGoalsPort;
import com.astra.learning.application.port.out.SaveGoalPort;
import com.astra.learning.domain.Goal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GoalPersistenceAdapter implements LoadGoalsPort, SaveGoalPort {

    private final SpringDataGoalRepository repository;

    GoalPersistenceAdapter(SpringDataGoalRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Goal> loadByUser(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(g -> new Goal(g.getUserId(), g.getType(), g.getTargetHours()))
                .toList();
    }

    @Override
    public void save(Goal goal) {
        GoalJpaEntity entity = repository.findByUserIdAndType(goal.userId(), goal.type())
                .orElseGet(() -> new GoalJpaEntity(goal.userId(), goal.type()));
        entity.setTargetHours(goal.targetHours());
        repository.save(entity);
    }
}
