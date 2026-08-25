package com.astra.roadmap.dto;

import com.astra.roadmap.StepStatus;

public record UpdateStepRequest(
        StepStatus status
) {
}
