package com.astra.user.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record DeleteOwnAccountCommand(UUID userId, String password, String confirmation) {

    public DeleteOwnAccountCommand {
        Objects.requireNonNull(userId);
    }
}
