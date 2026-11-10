package com.astra.call.domain;

import java.util.List;
import java.util.UUID;

public interface CallDirectory {

    boolean areFriends(UUID a, UUID b);

    List<UUID> groupMembers(UUID groupId);

    String groupName(UUID groupId);

    String nameOf(UUID userId);
}
