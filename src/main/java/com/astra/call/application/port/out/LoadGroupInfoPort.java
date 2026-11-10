package com.astra.call.application.port.out;

import java.util.List;
import java.util.UUID;

public interface LoadGroupInfoPort {

    List<UUID> memberIds(UUID groupId);

    String name(UUID groupId);
}
