package com.astra.social.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendshipResponse(
        UUID id,
        UUID friendUserId,
        String friendName,
        String status,
        boolean incoming,
        OffsetDateTime createdAt
) {
}
