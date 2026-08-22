package com.astra.roadmap;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoadmapStepCompletionRepository
        extends JpaRepository<RoadmapStepCompletion, RoadmapStepCompletionId> {

    boolean existsByUserIdAndStepId(UUID userId, UUID stepId);

    void deleteByUserIdAndStepId(UUID userId, UUID stepId);

    @Query("select c.stepId from RoadmapStepCompletion c where c.userId = :userId and c.stepId in :stepIds")
    List<UUID> findCompletedStepIds(@Param("userId") UUID userId, @Param("stepIds") Collection<UUID> stepIds);
}
