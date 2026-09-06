package com.astra.chat.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationSummary(
        UUID friendUserId,
        String friendName,
        String friendBio,
        boolean friendAdmin,
        String friendAccentColor,
        String friendProfileEffect,
        String lastMessage,
        OffsetDateTime lastMessageAt,
        long unreadCount) {
}
