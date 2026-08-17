package com.astra.roadmap.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStepRequest(
        @NotNull Boolean completed
) {
}
