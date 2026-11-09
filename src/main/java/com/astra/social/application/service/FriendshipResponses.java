package com.astra.social.application.service;

import com.astra.social.application.port.in.FriendshipResponse;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.social.application.port.out.LoadUserSummariesPort.UserSummary;
import com.astra.social.domain.Friendship;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class FriendshipResponses {

    private FriendshipResponses() {
    }

    static List<FriendshipResponse> of(Collection<Friendship> friendships, UUID viewerId,
                                       LoadUserSummariesPort loadUserSummariesPort) {
        if (friendships.isEmpty()) {
            return List.of();
        }
        Map<UUID, UserSummary> users = loadUserSummariesPort.loadByIds(
                friendships.stream().map(f -> f.otherUserId(viewerId)).toList());
        return friendships.stream().map(f -> of(f, viewerId, users)).toList();
    }

    static FriendshipResponse of(Friendship friendship, UUID viewerId, LoadUserSummariesPort loadUserSummariesPort) {
        return of(List.of(friendship), viewerId, loadUserSummariesPort).getFirst();
    }

    private static FriendshipResponse of(Friendship friendship, UUID viewerId, Map<UUID, UserSummary> users) {
        UUID otherId = friendship.otherUserId(viewerId);
        UserSummary other = users.get(otherId);
        return new FriendshipResponse(
                friendship.getId(),
                otherId,
                other != null ? other.name() : "",
                other != null ? other.bio() : null,
                other != null && other.admin(),
                friendship.getStatus().name(),
                friendship.isAddressee(viewerId),
                friendship.getCreatedAt());
    }
}
