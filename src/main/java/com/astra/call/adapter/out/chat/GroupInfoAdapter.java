package com.astra.call.adapter.out.chat;

import com.astra.call.application.port.out.LoadGroupInfoPort;
import com.astra.chat.application.port.in.GetGroupInfoQuery;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GroupInfoAdapter implements LoadGroupInfoPort {

    private final GetGroupInfoQuery getGroupInfoQuery;

    GroupInfoAdapter(GetGroupInfoQuery getGroupInfoQuery) {
        this.getGroupInfoQuery = getGroupInfoQuery;
    }

    @Override
    public List<UUID> memberIds(UUID groupId) {
        return getGroupInfoQuery.memberIds(groupId);
    }

    @Override
    public String name(UUID groupId) {
        return getGroupInfoQuery.name(groupId);
    }
}
