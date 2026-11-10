package com.astra.chat.adapter.in.websocket;

import com.astra.chat.application.port.in.SendDirectMessageCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record SendMessageRequest(
        @NotNull UUID recipientId,
        @Size(max = SendDirectMessageCommand.CONTENT_MAX_LENGTH) String content,
        UUID replyToMessageId
) {
}
