package com.astra.learning.dto;

import com.astra.learning.CourseStatus;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String title,
        String platform,
        CourseStatus status,
        double progress,
        long totalModules,
        long completedModules
) {
}
