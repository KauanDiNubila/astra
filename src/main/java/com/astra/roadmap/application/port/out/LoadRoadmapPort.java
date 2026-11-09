package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.Roadmap;
import java.util.Optional;
import java.util.UUID;

public interface LoadRoadmapPort {

    Optional<Roadmap> load(UUID roadmapId);

    Optional<Roadmap> loadOwnedByStep(UUID stepId, UUID userId);
}
