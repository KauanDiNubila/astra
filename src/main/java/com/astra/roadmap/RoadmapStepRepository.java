package com.astra.roadmap;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, UUID> {

    List<RoadmapStep> findByRoadmapIdOrderByPosition(UUID roadmapId);
}
