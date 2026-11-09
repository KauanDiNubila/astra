package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.AddLessonCommand;
import com.astra.learning.application.port.in.CreateCourseCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateLessonRequest(
        @NotBlank @Size(max = CreateCourseCommand.TITLE_MAX_LENGTH) String title,
        @NotNull @PositiveOrZero Integer position
) {

    AddLessonCommand toCommand(UUID userId, UUID courseId, UUID moduleId) {
        return new AddLessonCommand(userId, courseId, moduleId, title, position);
    }
}
