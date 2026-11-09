package com.astra.user.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record ChangePasswordCommand(UUID userId, String currentPassword, String newPassword) {

    public static final int PASSWORD_MIN_LENGTH = RegisterUserCommand.PASSWORD_MIN_LENGTH;
    public static final int PASSWORD_MAX_LENGTH = RegisterUserCommand.PASSWORD_MAX_LENGTH;

    public ChangePasswordCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(currentPassword);
        String password = newPassword;
        if (password == null || password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException("password must have between " + PASSWORD_MIN_LENGTH + " and "
                    + PASSWORD_MAX_LENGTH + " characters");
        }
    }
}
