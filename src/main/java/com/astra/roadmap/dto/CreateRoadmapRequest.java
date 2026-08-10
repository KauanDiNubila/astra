package com.astra.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoadmapRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 200) String source
) {
}
