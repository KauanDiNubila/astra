package com.astra.roadmap.dto;

import java.util.List;
import java.util.UUID;

public record RoadmapDetailResponse(
        UUID id,
        String title,
        String source,
        boolean predefined,
        List<StepResponse> steps
) {
}
