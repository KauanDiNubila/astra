package com.astra.roadmap.adapter.in.web;

import com.astra.roadmap.application.port.in.PinCourseCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record PinRequest(
        @NotNull UUID courseId,
        @Size(max = PinCourseCommand.STATUS_MAX_LENGTH) String status,
        @Min(PinCourseCommand.MIN_RATING) @Max(PinCourseCommand.MAX_RATING) Integer rating
) {

    PinCourseCommand toCommand(UUID userId, UUID stepId) {
        return new PinCourseCommand(userId, stepId, courseId, status, rating);
    }
}
