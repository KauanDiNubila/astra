package com.astra.learning.dto;

import com.astra.learning.CourseStatus;
import java.util.List;
import java.util.UUID;

public record CourseDetailResponse(
        UUID id,
        String title,
        String platform,
        CourseStatus status,
        double progress,
        long totalLessons,
        long completedLessons,
        List<ModuleResponse> modules
) {
}
