package com.astra.chat.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListGroupConversationsQuery {

    List<GroupConversationSummary> groups(UUID userId);
}
