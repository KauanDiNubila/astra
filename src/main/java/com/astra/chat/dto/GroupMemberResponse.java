package com.astra.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupMemberResponse(
        UUID userId,
        String name,
        OffsetDateTime joinedAt
) {
}
