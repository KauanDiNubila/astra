package com.astra.roadmap.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataStepRepository extends JpaRepository<StepJpaEntity, UUID> {

    @Query("select s from StepJpaEntity s join fetch s.roadmap where s.id = :id")
    Optional<StepJpaEntity> findWithRoadmap(@Param("id") UUID id);
}
