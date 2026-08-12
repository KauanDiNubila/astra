package com.astra.learning.dto;

import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        String title,
        int position,
        long totalLessons,
        long completedLessons,
        List<LessonResponse> lessons
) {
}
