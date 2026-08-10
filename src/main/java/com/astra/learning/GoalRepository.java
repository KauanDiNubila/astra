package com.astra.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByUserId(UUID userId);

    Optional<Goal> findByUserIdAndType(UUID userId, GoalType type);
}
