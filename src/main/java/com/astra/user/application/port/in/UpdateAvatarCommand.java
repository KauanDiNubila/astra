package com.astra.user.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record UpdateAvatarCommand(UUID userId, String contentType, byte[] bytes) {

    public UpdateAvatarCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(bytes);
    }
}
