package com.astra.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateModuleRequest(
        @NotBlank @Size(max = 160) String title,
        @NotNull @PositiveOrZero Integer position,
        @PositiveOrZero Integer lessonCount
) {
}
