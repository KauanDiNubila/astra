package com.astra.user.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String name, String bio) {

    public static final int NAME_MAX_LENGTH = RegisterUserCommand.NAME_MAX_LENGTH;
    public static final int BIO_MAX_LENGTH = 80;

    public UpdateProfileCommand {
        Objects.requireNonNull(userId);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > NAME_MAX_LENGTH || name.contains("#")) {
            throw new IllegalArgumentException("name must have at most " + NAME_MAX_LENGTH + " characters and no '#'");
        }
        if (bio != null && bio.length() > BIO_MAX_LENGTH) {
            throw new IllegalArgumentException("bio must have at most " + BIO_MAX_LENGTH + " characters");
        }
    }
}
