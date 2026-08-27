package com.astra.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID senderId,
        UUID recipientId,
        String content,
        OffsetDateTime createdAt,
        boolean read,
        UUID attachmentId,
        ReplyPreview replyTo) {

    public record ReplyPreview(UUID id, UUID senderId, String contentPreview) {
    }
}
