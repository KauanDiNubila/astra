package com.astra.chat.adapter.out.social;

import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.LoadFriendIdsPort;
import com.astra.social.application.port.in.CheckFriendshipQuery;
import com.astra.social.application.port.in.GetFriendIdsQuery;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FriendshipAdapter implements CheckFriendshipPort, LoadFriendIdsPort {

    private final CheckFriendshipQuery checkFriendshipQuery;
    private final GetFriendIdsQuery getFriendIdsQuery;

    FriendshipAdapter(CheckFriendshipQuery checkFriendshipQuery, GetFriendIdsQuery getFriendIdsQuery) {
        this.checkFriendshipQuery = checkFriendshipQuery;
        this.getFriendIdsQuery = getFriendIdsQuery;
    }

    @Override
    public boolean areFriends(UUID a, UUID b) {
        return checkFriendshipQuery.areFriends(a, b);
    }

    @Override
    public List<UUID> friendIdsOf(UUID userId) {
        return getFriendIdsQuery.friendIdsOf(userId);
    }
}
