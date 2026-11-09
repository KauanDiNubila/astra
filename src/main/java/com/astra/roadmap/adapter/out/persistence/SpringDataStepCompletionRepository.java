package com.astra.roadmap.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataStepCompletionRepository
        extends JpaRepository<StepCompletionJpaEntity, StepCompletionJpaEntity.Key> {

    @Query("select c from StepCompletionJpaEntity c where c.userId = :userId and c.stepId in :stepIds")
    List<StepCompletionJpaEntity> findForUser(@Param("userId") UUID userId,
                                              @Param("stepIds") Collection<UUID> stepIds);

    List<StepCompletionJpaEntity> findByUserId(UUID userId);
}
