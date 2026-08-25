package com.astra.roadmap;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapStepResourceRepository extends JpaRepository<RoadmapStepResource, UUID> {

    List<RoadmapStepResource> findByStepIdInOrderByPosition(Collection<UUID> stepIds);

    List<RoadmapStepResource> findByStepIdOrderByPosition(UUID stepId);

    void deleteByIdAndStepId(UUID id, UUID stepId);
}
