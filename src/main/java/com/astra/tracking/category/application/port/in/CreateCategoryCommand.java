package com.astra.tracking.category.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record CreateCategoryCommand(UUID userId, String name, String color) {

    public static final int NAME_MAX_LENGTH = 80;
    public static final int COLOR_MAX_LENGTH = 20;

    public CreateCategoryCommand {
        Objects.requireNonNull(userId);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("name must have at most " + NAME_MAX_LENGTH + " characters");
        }
        if (color != null && color.length() > COLOR_MAX_LENGTH) {
            throw new IllegalArgumentException("color must have at most " + COLOR_MAX_LENGTH + " characters");
        }
    }
}
