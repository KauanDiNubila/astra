package com.astra.call.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record StartCallCommand(UUID userId, String sessionId, UUID targetUserId, UUID groupId, String clientId) {

    public static final int CLIENT_ID_MAX_LENGTH = 64;

    public StartCallCommand {
        Objects.requireNonNull(userId);
        if (clientId == null || clientId.isBlank() || clientId.length() > CLIENT_ID_MAX_LENGTH) {
            throw new IllegalArgumentException("clientId must not be blank and have at most "
                    + CLIENT_ID_MAX_LENGTH + " characters");
        }
    }
}
