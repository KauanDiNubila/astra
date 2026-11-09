package com.astra.learning.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record CreateCourseCommand(UUID userId, String title, String platform, int moduleCount) {

    public static final int TITLE_MAX_LENGTH = 160;
    public static final int PLATFORM_MAX_LENGTH = 80;

    public CreateCourseCommand {
        Objects.requireNonNull(userId);
        requireTitle(title);
        if (platform != null && platform.length() > PLATFORM_MAX_LENGTH) {
            throw new IllegalArgumentException("platform must have at most " + PLATFORM_MAX_LENGTH + " characters");
        }
        if (moduleCount < 0) {
            throw new IllegalArgumentException("moduleCount must not be negative");
        }
    }

    static void requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        requireTitleLength(title);
    }

    static void requireTitleLength(String title) {
        if (title != null && title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("title must have at most " + TITLE_MAX_LENGTH + " characters");
        }
    }

    static void requireNotNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
