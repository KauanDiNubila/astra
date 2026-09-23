package com.astra.roadmap.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.roadmap.entity.RoadmapStep;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, UUID> {

    List<RoadmapStep> findByRoadmapIdOrderByPosition(UUID roadmapId);
}
