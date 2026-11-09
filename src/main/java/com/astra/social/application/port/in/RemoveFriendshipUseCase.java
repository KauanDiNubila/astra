package com.astra.social.application.port.in;

import java.util.UUID;

public interface RemoveFriendshipUseCase {

    void remove(UUID userId, UUID friendshipId);
}
