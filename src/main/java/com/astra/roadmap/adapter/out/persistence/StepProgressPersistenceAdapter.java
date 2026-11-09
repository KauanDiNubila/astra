package com.astra.roadmap.adapter.out.persistence;

import com.astra.roadmap.application.port.out.LoadStepStatusesPort;
import com.astra.roadmap.application.port.out.SaveStepStatusPort;
import com.astra.roadmap.domain.StepProgress;
import com.astra.roadmap.domain.StepStatus;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class StepProgressPersistenceAdapter implements LoadStepStatusesPort, SaveStepStatusPort {

    private final SpringDataStepCompletionRepository repository;
    private final EntityManager entityManager;

    StepProgressPersistenceAdapter(SpringDataStepCompletionRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Map<UUID, StepStatus> statusesOf(UUID userId, Collection<UUID> stepIds) {
        return repository.findForUser(userId, stepIds).stream()
                .collect(Collectors.toMap(StepCompletionJpaEntity::getStepId, StepCompletionJpaEntity::getStatus));
    }

    @Override
    public List<StepProgress> listByUser(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(c -> new StepProgress(c.getUserId(), c.getStepId(), c.getStatus(), c.getCompletedAt()))
                .toList();
    }

    @Override
    public void save(UUID userId, UUID stepId, StepStatus status) {
        StepCompletionJpaEntity existing = entityManager.find(StepCompletionJpaEntity.class,
                new StepCompletionJpaEntity.Key(userId, stepId));
        if (existing != null) {
            entityManager.remove(existing);
            entityManager.flush();
        }
        if (status != null) {
            entityManager.persist(new StepCompletionJpaEntity(userId, stepId, status));
        }
    }
}
