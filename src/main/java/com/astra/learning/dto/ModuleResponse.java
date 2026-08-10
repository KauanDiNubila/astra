package com.astra.learning.dto;

import java.util.UUID;

public record ModuleResponse(
        UUID id,
        String title,
        int position,
        boolean completed
) {
}
