package com.astra.roadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddResourceRequest(
        @NotBlank @Size(max = 160) String label,
        @NotBlank @Size(max = 500) String url
) {
}
