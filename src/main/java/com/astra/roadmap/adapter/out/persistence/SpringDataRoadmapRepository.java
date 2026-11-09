package com.astra.roadmap.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataRoadmapRepository extends JpaRepository<RoadmapJpaEntity, UUID> {

    List<RoadmapJpaEntity> findByOwnerIdIsNullOrOwnerId(UUID ownerId);

    List<RoadmapJpaEntity> findByOwnerId(UUID ownerId);

    @Query("select r from RoadmapJpaEntity r join r.steps s where s.id = :stepId and r.ownerId = :userId")
    Optional<RoadmapJpaEntity> findOwnedByStep(@Param("stepId") UUID stepId, @Param("userId") UUID userId);
}
