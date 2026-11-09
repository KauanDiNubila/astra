package com.astra.social.adapter.out.persistence;

import com.astra.social.domain.Friendship;
import com.astra.social.domain.FriendshipStatus;
import org.springframework.stereotype.Component;

@Component
class FriendshipMapper {

    Friendship toDomain(FriendshipJpaEntity entity) {
        return Friendship.withId(entity.getId(), entity.getRequesterId(), entity.getAddresseeId(),
                FriendshipStatus.valueOf(entity.getStatus()), entity.getCreatedAt());
    }

    FriendshipJpaEntity toJpaEntity(Friendship friendship) {
        return new FriendshipJpaEntity(friendship.getId(), friendship.getRequesterId(), friendship.getAddresseeId(),
                friendship.getStatus().name(), friendship.getCreatedAt());
    }
}
