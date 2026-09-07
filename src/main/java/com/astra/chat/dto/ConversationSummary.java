package com.astra.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationSummary(
        UUID friendUserId,
        String friendName,
        String friendTag,
        String friendBio,
        boolean friendAdmin,
        String friendGithubLogin,
        String lastMessage,
        OffsetDateTime lastMessageAt,
        long unreadCount) {
}
