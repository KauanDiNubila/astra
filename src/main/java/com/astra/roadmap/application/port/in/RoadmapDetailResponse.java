package com.astra.roadmap.application.port.in;

import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.StepStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RoadmapDetailResponse(UUID id, String title, String source, boolean predefined,
                                    List<StepResponse> steps) {

    public static RoadmapDetailResponse from(Roadmap roadmap, Map<UUID, StepStatus> statusByStep) {
        return new RoadmapDetailResponse(roadmap.getId(), roadmap.getTitle(), roadmap.getSource(),
                roadmap.getOwnerId() == null,
                roadmap.getSteps().stream()
                        .map(step -> StepResponse.from(step, statusByStep.get(step.getId())))
                        .toList());
    }
}
