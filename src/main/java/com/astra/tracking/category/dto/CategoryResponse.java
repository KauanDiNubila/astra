package com.astra.tracking.category.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String color
) {
}
