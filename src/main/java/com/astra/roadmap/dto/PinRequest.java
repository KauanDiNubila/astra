package com.astra.roadmap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PinRequest(
        @NotNull UUID courseId,
        @Size(max = 30) String status,
        @Min(1) @Max(5) Integer rating
) {
}
