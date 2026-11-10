package com.astra.chat.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListMessagesQuery {

    List<MessageResponse> directHistory(UUID userId, UUID friendId, int limit);

    List<MessageResponse> groupHistory(UUID userId, UUID groupId, int limit);

    List<MessageResponse> directPinned(UUID userId, UUID friendId);

    List<MessageResponse> groupPinned(UUID userId, UUID groupId);
}
