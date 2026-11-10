package com.astra.chat.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record SendGroupMessageCommand(UUID senderId, UUID groupId, String content, UUID replyToMessageId,
                                      ImageUpload image) {

    public SendGroupMessageCommand {
        Objects.requireNonNull(senderId);
        Objects.requireNonNull(groupId);
        if (content != null && content.length() > SendDirectMessageCommand.CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("content must have at most "
                    + SendDirectMessageCommand.CONTENT_MAX_LENGTH + " characters");
        }
    }
}
