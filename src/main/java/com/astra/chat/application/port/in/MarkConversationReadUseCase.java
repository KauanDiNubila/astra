package com.astra.chat.application.port.in;

import java.util.UUID;

public interface MarkConversationReadUseCase {

    void markRead(UUID userId, UUID friendId);
}
