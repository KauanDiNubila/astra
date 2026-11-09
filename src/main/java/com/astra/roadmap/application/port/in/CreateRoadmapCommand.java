package com.astra.roadmap.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record CreateRoadmapCommand(UUID userId, String title, String source) {

    public static final int TITLE_MAX_LENGTH = 160;
    public static final int SOURCE_MAX_LENGTH = 200;

    public CreateRoadmapCommand {
        Objects.requireNonNull(userId);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (title != null && title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("title must have at most " + TITLE_MAX_LENGTH + " characters");
        }
        if (source != null && source.length() > SOURCE_MAX_LENGTH) {
            throw new IllegalArgumentException("source must have at most " + SOURCE_MAX_LENGTH + " characters");
        }
    }
}
