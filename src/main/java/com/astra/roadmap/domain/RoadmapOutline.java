package com.astra.roadmap.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RoadmapOutline(UUID id, UUID ownerId, String title, String source, List<StepNode> steps) {

    public RoadmapOutline {
        steps = List.copyOf(steps);
    }

    public boolean isPredefined() {
        return ownerId == null;
    }

    public boolean isAccessibleBy(UUID userId) {
        return isPredefined() || ownerId.equals(userId);
    }

    public List<UUID> stepIds() {
        return steps.stream().map(StepNode::id).toList();
    }

    public RoadmapProgress progress(Map<UUID, StepStatus> statusByStep) {
        return RoadmapProgress.of(steps, statusByStep);
    }
}
