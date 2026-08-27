package com.astra.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendMessageRequest(
        @NotNull UUID recipientId,
        @Size(max = 2000) String content,
        UUID replyToMessageId) {
}
