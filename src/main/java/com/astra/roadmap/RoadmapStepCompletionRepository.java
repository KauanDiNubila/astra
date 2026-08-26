package com.astra.roadmap;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoadmapStepCompletionRepository
        extends JpaRepository<RoadmapStepCompletion, RoadmapStepCompletionId> {

    Optional<RoadmapStepCompletion> findByUserIdAndStepId(UUID userId, UUID stepId);

    // @Modifying + delete direto: a versão derivada (deleteByUserIdAndStepId)
    // faz um SELECT antes do DELETE; aqui é uma instrução só.
    @Modifying
    @Query("delete from RoadmapStepCompletion c where c.userId = :userId and c.stepId = :stepId")
    void deleteByUserIdAndStepId(@Param("userId") UUID userId, @Param("stepId") UUID stepId);

    @Query("select c from RoadmapStepCompletion c where c.userId = :userId and c.stepId in :stepIds")
    List<RoadmapStepCompletion> findStatusesForUser(@Param("userId") UUID userId,
                                                      @Param("stepIds") Collection<UUID> stepIds);
}
