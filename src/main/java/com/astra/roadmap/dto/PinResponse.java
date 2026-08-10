package com.astra.roadmap.dto;

import java.util.UUID;

public record PinResponse(
        UUID id,
        UUID courseId,
        UUID stepId,
        String status,
        Integer rating
) {
}
