package com.astra.chat.application.port.in;

import java.util.List;
import java.util.UUID;

public interface GetGroupInfoQuery {

    List<UUID> memberIds(UUID groupId);

    String name(UUID groupId);
}
