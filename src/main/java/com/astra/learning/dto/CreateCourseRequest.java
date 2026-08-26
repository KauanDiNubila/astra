package com.astra.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 80) String platform,
        @PositiveOrZero Integer moduleCount
) {
}
