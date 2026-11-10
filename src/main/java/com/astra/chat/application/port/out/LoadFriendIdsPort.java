package com.astra.chat.application.port.out;

import java.util.List;
import java.util.UUID;

public interface LoadFriendIdsPort {

    List<UUID> friendIdsOf(UUID userId);
}
