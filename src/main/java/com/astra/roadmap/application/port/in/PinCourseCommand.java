package com.astra.roadmap.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record PinCourseCommand(UUID userId, UUID stepId, UUID courseId, String status, Integer rating) {

    public static final int STATUS_MAX_LENGTH = 30;
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    public PinCourseCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(stepId);
        Objects.requireNonNull(courseId);
        if (status != null && status.length() > STATUS_MAX_LENGTH) {
            throw new IllegalArgumentException("status must have at most " + STATUS_MAX_LENGTH + " characters");
        }
        if (rating != null && (rating < MIN_RATING || rating > MAX_RATING)) {
            throw new IllegalArgumentException("rating must be between " + MIN_RATING + " and " + MAX_RATING);
        }
    }
}
