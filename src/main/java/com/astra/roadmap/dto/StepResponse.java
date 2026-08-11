package com.astra.roadmap.dto;

import java.util.UUID;

public record StepResponse(
        UUID id,
        String title,
        int position,
        UUID parentStepId
) {
}
