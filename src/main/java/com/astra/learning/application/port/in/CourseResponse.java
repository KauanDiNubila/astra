package com.astra.learning.application.port.in;

import com.astra.learning.domain.Course;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String title,
        String platform,
        String status,
        double progress,
        long totalLessons,
        long completedLessons,
        OffsetDateTime lastStudiedAt
) {

    public static CourseResponse from(Course course, OffsetDateTime lastStudiedAt) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getPlatform(),
                statusOf(course), course.progress(), course.totalLessons(), course.completedLessons(),
                lastStudiedAt);
    }

    static String statusOf(Course course) {
        return course.getStatus() == null ? null : course.getStatus().name();
    }
}
