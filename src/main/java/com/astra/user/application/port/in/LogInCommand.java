package com.astra.user.application.port.in;

import java.util.Objects;

public record LogInCommand(String email, String password) {

    public LogInCommand {
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
    }
}
