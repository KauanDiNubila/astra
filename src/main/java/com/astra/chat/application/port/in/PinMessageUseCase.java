package com.astra.chat.application.port.in;

import java.util.UUID;

public interface PinMessageUseCase {

    PinChange setPinned(UUID userId, UUID messageId, boolean pinned);
}
