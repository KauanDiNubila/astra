package com.astra.roadmap.dto;

import com.astra.roadmap.entity.StepStatus;

public record UpdateStepRequest(
        StepStatus status
) {
}
