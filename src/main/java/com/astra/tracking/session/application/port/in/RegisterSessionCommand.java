package com.astra.tracking.session.application.port.in;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record RegisterSessionCommand(
        UUID userId,
        UUID categoryId,
        UUID courseId,
        int focusedMinutes,
        OffsetDateTime startedAt,
        String note
) {

    public static final int NOTE_MAX_LENGTH = 500;

    public RegisterSessionCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(categoryId);
        Objects.requireNonNull(startedAt);
        if (focusedMinutes <= 0) {
            throw new IllegalArgumentException("focusedMinutes must be positive");
        }
        if (note != null && note.length() > NOTE_MAX_LENGTH) {
            throw new IllegalArgumentException("note must have at most " + NOTE_MAX_LENGTH + " characters");
        }
    }
}
