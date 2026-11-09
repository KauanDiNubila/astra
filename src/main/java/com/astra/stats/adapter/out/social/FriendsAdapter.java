package com.astra.stats.adapter.out.social;

import com.astra.social.application.port.in.GetFriendIdsQuery;
import com.astra.stats.application.port.out.LoadFriendIdsPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FriendsAdapter implements LoadFriendIdsPort {

    private final GetFriendIdsQuery getFriendIdsQuery;

    FriendsAdapter(GetFriendIdsQuery getFriendIdsQuery) {
        this.getFriendIdsQuery = getFriendIdsQuery;
    }

    @Override
    public List<UUID> friendIdsOf(UUID userId) {
        return getFriendIdsQuery.friendIdsOf(userId);
    }
}
