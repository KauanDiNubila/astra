package com.astra.roadmap.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record AddStepCommand(UUID userId, UUID roadmapId, String title, int position, UUID parentStepId,
                             String description) {

    public static final int TITLE_MAX_LENGTH = 160;
    public static final int DESCRIPTION_MAX_LENGTH = 2000;

    public AddStepCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(roadmapId);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (title != null && title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("title must have at most " + TITLE_MAX_LENGTH + " characters");
        }
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("description must have at most " + DESCRIPTION_MAX_LENGTH + " characters");
        }
        if (position < 0) {
            throw new IllegalArgumentException("position must not be negative");
        }
    }
}
