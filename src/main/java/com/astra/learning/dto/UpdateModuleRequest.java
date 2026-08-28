package com.astra.learning.dto;

import jakarta.validation.constraints.Size;

public record UpdateModuleRequest(
        @Size(max = 160) String title,
        Boolean completed
) {
}
