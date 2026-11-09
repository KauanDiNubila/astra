package com.astra.learning.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record AddModuleCommand(UUID userId, UUID courseId, String title, int position, int lessonCount) {

    public AddModuleCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(courseId);
        CreateCourseCommand.requireTitle(title);
        CreateCourseCommand.requireNotNegative(position, "position");
        CreateCourseCommand.requireNotNegative(lessonCount, "lessonCount");
    }
}
