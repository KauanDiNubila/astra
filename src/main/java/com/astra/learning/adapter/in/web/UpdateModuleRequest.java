package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.CreateCourseCommand;
import com.astra.learning.application.port.in.UpdateModuleCommand;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record UpdateModuleRequest(
        @Size(max = CreateCourseCommand.TITLE_MAX_LENGTH) String title,
        Boolean completed
) {

    UpdateModuleCommand toCommand(UUID userId, UUID courseId, UUID moduleId) {
        return new UpdateModuleCommand(userId, courseId, moduleId, title, completed);
    }
}
