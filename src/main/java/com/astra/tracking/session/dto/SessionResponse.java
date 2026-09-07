package com.astra.tracking.session.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID categoryId,
        UUID courseId,
        UUID githubRepositoryId,
        int focusedMinutes,
        OffsetDateTime startedAt,
        String note,
        OffsetDateTime createdAt
) {
}
