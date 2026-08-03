package com.astra.tracking.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 20) String color
) {
}
