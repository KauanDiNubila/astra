package com.astra.roadmap.domain;

import java.util.Objects;
import java.util.UUID;

public record LocatedStep(UUID roadmapId, UUID ownerId, RoadmapStep step) {

    public LocatedStep {
        Objects.requireNonNull(roadmapId);
        Objects.requireNonNull(step);
    }

    public boolean isAccessibleBy(UUID userId) {
        return ownerId == null || ownerId.equals(userId);
    }

    public boolean isIn(UUID roadmapId) {
        return this.roadmapId.equals(roadmapId);
    }
}
