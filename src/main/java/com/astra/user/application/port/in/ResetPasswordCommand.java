package com.astra.user.application.port.in;

import java.util.Objects;

public record ResetPasswordCommand(String token, String newPassword) {

    public static final int PASSWORD_MIN_LENGTH = RegisterUserCommand.PASSWORD_MIN_LENGTH;
    public static final int PASSWORD_MAX_LENGTH = RegisterUserCommand.PASSWORD_MAX_LENGTH;

    public ResetPasswordCommand {
        Objects.requireNonNull(token);
        if (newPassword == null || newPassword.length() < PASSWORD_MIN_LENGTH
                || newPassword.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException("password must have between " + PASSWORD_MIN_LENGTH + " and "
                    + PASSWORD_MAX_LENGTH + " characters");
        }
    }
}
