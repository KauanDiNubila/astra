package com.astra.call.adapter.out.social;

import com.astra.call.application.port.out.CheckFriendshipPort;
import com.astra.social.application.port.in.CheckFriendshipQuery;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FriendCheckAdapter implements CheckFriendshipPort {

    private final CheckFriendshipQuery checkFriendshipQuery;

    FriendCheckAdapter(CheckFriendshipQuery checkFriendshipQuery) {
        this.checkFriendshipQuery = checkFriendshipQuery;
    }

    @Override
    public boolean areFriends(UUID a, UUID b) {
        return checkFriendshipQuery.areFriends(a, b);
    }
}
