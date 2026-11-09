package com.astra.roadmap.application.port.in;

import com.astra.roadmap.domain.RoadmapStep;
import com.astra.roadmap.domain.StepStatus;
import java.util.List;
import java.util.UUID;

public record StepResponse(UUID id, String title, int position, UUID parentStepId, String status,
                           String description, List<ResourceResponse> resources) {

    public static StepResponse from(RoadmapStep step, StepStatus status) {
        return new StepResponse(step.getId(), step.getTitle(), step.getPosition(), step.getParentStepId(),
                status == null ? null : status.name(), step.getDescription(),
                step.getResources().stream().map(ResourceResponse::from).toList());
    }
}
