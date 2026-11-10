package com.astra.chat.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface MarkGroupReadPort {

    boolean markRead(UUID groupId, UUID userId, OffsetDateTime now);
}
