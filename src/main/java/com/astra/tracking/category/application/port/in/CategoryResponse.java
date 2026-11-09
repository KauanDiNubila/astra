package com.astra.tracking.category.application.port.in;

import com.astra.tracking.category.domain.Category;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String color
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}
