package com.astra.roadmap.application.port.in;

import com.astra.roadmap.domain.CoursePin;
import java.util.UUID;

public record PinResponse(UUID id, UUID courseId, UUID stepId, String status, Integer rating) {

    public static PinResponse from(CoursePin pin) {
        return new PinResponse(pin.getId(), pin.getCourseId(), pin.getStepId(), pin.getStatus(), pin.getRating());
    }
}
