package com.astra.roadmap.dto;

import java.util.UUID;

public record RoadmapResponse(
        UUID id,
        String title,
        String source,
        boolean predefined
) {
}
