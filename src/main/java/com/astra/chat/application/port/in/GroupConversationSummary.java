package com.astra.chat.application.port.in;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record GroupConversationSummary(UUID groupId, String groupName, List<String> memberNames, String lastMessage,
                                       OffsetDateTime lastMessageAt, long unreadCount) {
}
