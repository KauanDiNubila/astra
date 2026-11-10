package com.astra.chat.application.port.out;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadLastDirectMessagesPort {

    Map<UUID, LastMessage> lastWith(UUID userId, Collection<UUID> friendIds);

    record LastMessage(String content, OffsetDateTime createdAt) {
    }
}
