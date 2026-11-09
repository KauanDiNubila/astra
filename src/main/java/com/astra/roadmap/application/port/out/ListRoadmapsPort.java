package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.RoadmapOutline;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListRoadmapsPort {

    List<RoadmapOutline> listOutlinesFor(UUID userId);

    Optional<RoadmapOutline> loadOutline(UUID roadmapId);

    List<Roadmap> listOwnedBy(UUID userId);
}
