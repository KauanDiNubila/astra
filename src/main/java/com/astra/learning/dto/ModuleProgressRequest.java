package com.astra.learning.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ModuleProgressRequest(
        @NotNull @PositiveOrZero Integer uptoPosition
) {
}
