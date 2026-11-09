package com.astra.tracking.category.application.port.in;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String color
) {
}
