package com.astra.learning.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.learning.entity.Goal;
import com.astra.learning.entity.GoalType;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByUserId(UUID userId);

    Optional<Goal> findByUserIdAndType(UUID userId, GoalType type);
}
