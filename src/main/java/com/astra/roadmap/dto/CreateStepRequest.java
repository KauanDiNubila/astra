package com.astra.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateStepRequest(
        @NotBlank @Size(max = 160) String title,
        @NotNull @PositiveOrZero Integer position,
        UUID parentStepId
) {
}
