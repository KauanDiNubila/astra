package com.astra.learning.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateModuleRequest(
        @NotNull Boolean completed
) {
}
