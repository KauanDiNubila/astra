package com.astra.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendGroupMessageRequest(
        @NotNull UUID groupId,
        @Size(max = 2000) String content,
        UUID replyToMessageId
) {
}
