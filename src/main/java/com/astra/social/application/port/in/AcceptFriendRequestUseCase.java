package com.astra.social.application.port.in;

import java.util.UUID;

public interface AcceptFriendRequestUseCase {

    FriendshipResponse accept(UUID userId, UUID friendshipId);
}
