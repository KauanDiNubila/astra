package com.astra.chat.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CountUnreadPort {

    long directTo(UUID userId);

    Map<UUID, Long> directBySender(UUID userId, Collection<UUID> friendIds);

    Map<UUID, Long> inGroups(UUID userId, Collection<UUID> groupIds);
}
