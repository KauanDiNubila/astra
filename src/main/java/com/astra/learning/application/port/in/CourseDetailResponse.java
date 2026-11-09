package com.astra.learning.application.port.in;

import com.astra.learning.domain.Course;
import java.util.List;
import java.util.UUID;

public record CourseDetailResponse(
        UUID id,
        String title,
        String platform,
        String status,
        double progress,
        long totalLessons,
        long completedLessons,
        List<ModuleResponse> modules
) {

    public static CourseDetailResponse from(Course course) {
        return new CourseDetailResponse(course.getId(), course.getTitle(), course.getPlatform(),
                CourseResponse.statusOf(course), course.progress(), course.totalLessons(), course.completedLessons(),
                course.getModules().stream().map(ModuleResponse::from).toList());
    }
}
