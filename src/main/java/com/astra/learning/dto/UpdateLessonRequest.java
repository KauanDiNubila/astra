package com.astra.learning.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateLessonRequest(
        @NotNull Boolean completed
) {
}
