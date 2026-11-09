package com.astra.learning.application.port.in;

import com.astra.learning.domain.CourseModule;
import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        String title,
        int position,
        long totalLessons,
        long completedLessons,
        boolean completed,
        List<LessonResponse> lessons
) {

    public static ModuleResponse from(CourseModule module) {
        return new ModuleResponse(module.getId(), module.getTitle(), module.getPosition(), module.totalLessons(),
                module.completedLessons(), module.isCompleted(),
                module.getLessons().stream().map(LessonResponse::from).toList());
    }
}
