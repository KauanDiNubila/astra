package com.astra.chat.application.port.in;

import java.util.UUID;

public interface PinMessageUseCase {

    MessageResponse setPinned(UUID userId, UUID messageId, boolean pinned);
}
