package com.astra.roadmap.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.roadmap.entity.RoadmapStepResource;

public interface RoadmapStepResourceRepository extends JpaRepository<RoadmapStepResource, UUID> {

    List<RoadmapStepResource> findByStepIdInOrderByPosition(Collection<UUID> stepIds);

    List<RoadmapStepResource> findByStepIdOrderByPosition(UUID stepId);

    void deleteByIdAndStepId(UUID id, UUID stepId);
}
