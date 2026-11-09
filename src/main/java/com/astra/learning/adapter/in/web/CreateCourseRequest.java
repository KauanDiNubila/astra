package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.CreateCourseCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateCourseRequest(
        @NotBlank @Size(max = CreateCourseCommand.TITLE_MAX_LENGTH) String title,
        @Size(max = CreateCourseCommand.PLATFORM_MAX_LENGTH) String platform,
        @PositiveOrZero Integer moduleCount
) {

    CreateCourseCommand toCommand(UUID userId) {
        return new CreateCourseCommand(userId, title, platform, moduleCount == null ? 0 : moduleCount);
    }
}
