package com.astra.learning.dto;

import java.util.UUID;

public record LessonResponse(
        UUID id,
        String title,
        int position,
        boolean completed
) {
}
