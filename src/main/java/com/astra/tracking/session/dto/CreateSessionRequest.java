package com.astra.tracking.session.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateSessionRequest(
        @NotNull UUID categoryId,
        @NotNull @Positive Integer focusedMinutes,
        @NotNull OffsetDateTime startedAt,
        @Size(max = 500) String note
) {
}
