package com.astra.chat.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record SendDirectMessageCommand(UUID senderId, UUID recipientId, String content, UUID replyToMessageId,
                                       ImageUpload image) {

    public static final int CONTENT_MAX_LENGTH = 2000;

    public SendDirectMessageCommand {
        Objects.requireNonNull(senderId);
        Objects.requireNonNull(recipientId);
        if (content != null && content.length() > CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("content must have at most " + CONTENT_MAX_LENGTH + " characters");
        }
    }
}
