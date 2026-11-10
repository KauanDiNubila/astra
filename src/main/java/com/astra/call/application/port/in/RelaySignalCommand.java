package com.astra.call.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record RelaySignalCommand(UUID userId, UUID callId, String toClient, String type, String data) {

    public static final int DATA_MAX_LENGTH = 60000;

    public RelaySignalCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(callId);
        Objects.requireNonNull(toClient);
        Objects.requireNonNull(type);
        if (data == null || data.isBlank() || data.length() > DATA_MAX_LENGTH) {
            throw new IllegalArgumentException("data must not be blank and have at most " + DATA_MAX_LENGTH
                    + " characters");
        }
    }
}
