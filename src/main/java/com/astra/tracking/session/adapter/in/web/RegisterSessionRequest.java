package com.astra.tracking.session.adapter.in.web;

import com.astra.tracking.session.application.port.in.RegisterSessionCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

record RegisterSessionRequest(
        @NotNull UUID categoryId,
        UUID courseId,
        @NotNull @Positive Integer focusedMinutes,
        @NotNull OffsetDateTime startedAt,
        @Size(max = RegisterSessionCommand.NOTE_MAX_LENGTH) String note
) {

    RegisterSessionCommand toCommand(UUID userId) {
        return new RegisterSessionCommand(userId, categoryId, courseId, focusedMinutes, startedAt, note);
    }
}
