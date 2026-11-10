package com.astra.chat.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface MarkConversationReadPort {

    void markRead(UUID senderId, UUID recipientId, OffsetDateTime now);
}
