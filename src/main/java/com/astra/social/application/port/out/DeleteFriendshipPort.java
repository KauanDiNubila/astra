package com.astra.social.application.port.out;

import java.util.UUID;

public interface DeleteFriendshipPort {

    void delete(UUID friendshipId);
}
