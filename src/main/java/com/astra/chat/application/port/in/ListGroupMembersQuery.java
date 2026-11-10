package com.astra.chat.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListGroupMembersQuery {

    List<GroupMemberResponse> members(UUID userId, UUID groupId);
}
