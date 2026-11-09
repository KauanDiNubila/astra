package com.astra.learning.adapter.out.persistence;

import com.astra.learning.domain.GoalType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGoalRepository extends JpaRepository<GoalJpaEntity, UUID> {

    List<GoalJpaEntity> findByUserId(UUID userId);

    Optional<GoalJpaEntity> findByUserIdAndType(UUID userId, GoalType type);
}
