package com.astra.social.application.port.in;

import java.util.UUID;

public interface CheckFriendshipQuery {

    boolean areFriends(UUID userId, UUID otherUserId);
}
