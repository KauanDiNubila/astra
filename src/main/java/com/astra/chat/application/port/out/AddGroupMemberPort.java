package com.astra.chat.application.port.out;

import com.astra.chat.domain.GroupMember;
import java.util.UUID;

public interface AddGroupMemberPort {

    void add(UUID groupId, GroupMember member);
}
