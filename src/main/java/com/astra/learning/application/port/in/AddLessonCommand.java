package com.astra.learning.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record AddLessonCommand(UUID userId, UUID courseId, UUID moduleId, String title, int position) {

    public AddLessonCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(moduleId);
        CreateCourseCommand.requireTitle(title);
        CreateCourseCommand.requireNotNegative(position, "position");
    }
}
