package com.astra.chat.application.port.in;

import java.util.UUID;

public interface MarkGroupReadUseCase {

    void markRead(UUID userId, UUID groupId);
}
