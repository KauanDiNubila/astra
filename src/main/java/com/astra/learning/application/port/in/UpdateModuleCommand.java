package com.astra.learning.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record UpdateModuleCommand(UUID userId, UUID courseId, UUID moduleId, String title, Boolean completed) {

    public UpdateModuleCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(moduleId);
        CreateCourseCommand.requireTitleLength(title);
    }
}
