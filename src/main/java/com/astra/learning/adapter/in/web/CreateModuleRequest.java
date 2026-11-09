package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.AddModuleCommand;
import com.astra.learning.application.port.in.CreateCourseCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateModuleRequest(
        @NotBlank @Size(max = CreateCourseCommand.TITLE_MAX_LENGTH) String title,
        @NotNull @PositiveOrZero Integer position,
        @PositiveOrZero Integer lessonCount
) {

    AddModuleCommand toCommand(UUID userId, UUID courseId) {
        return new AddModuleCommand(userId, courseId, title, position, lessonCount == null ? 0 : lessonCount);
    }
}
