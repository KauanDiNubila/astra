package com.astra.chat.application.port.out;

import com.astra.chat.domain.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListMessagesPort {

    List<Message> directConversation(UUID a, UUID b, int limit);

    List<Message> group(UUID groupId, int limit);

    List<Message> pinnedDirect(UUID a, UUID b);

    List<Message> pinnedInGroup(UUID groupId);

    Optional<Message> lastInGroup(UUID groupId);

    List<Message> forExport(UUID userId);
}
