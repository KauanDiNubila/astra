package com.astra.chat.application.port.in;

import java.util.UUID;

public interface AddGroupMemberUseCase {

    void add(UUID userId, UUID groupId, UUID newMemberId);
}
