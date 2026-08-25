package com.astra.roadmap.dto;

import com.astra.roadmap.StepStatus;
import java.util.List;
import java.util.UUID;

public record StepResponse(
        UUID id,
        String title,
        int position,
        UUID parentStepId,
        StepStatus status,
        String description,
        List<ResourceResponse> resources
) {
}
