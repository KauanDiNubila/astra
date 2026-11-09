package com.astra.social.application.port.out;

import com.astra.social.domain.Friendship;
import java.util.Optional;
import java.util.UUID;

public interface LoadFriendshipPort {

    Optional<Friendship> load(UUID friendshipId);

    Optional<Friendship> loadBetween(UUID userId, UUID otherUserId);
}
