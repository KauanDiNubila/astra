package com.astra.chat.application.port.out;

import java.util.List;
import java.util.UUID;

public interface CheckGroupMemberPort {

    boolean isMember(UUID groupId, UUID userId);

    List<UUID> memberIds(UUID groupId);

    List<UUID> groupIdsOf(UUID userId);
}
