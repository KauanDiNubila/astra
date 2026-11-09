package com.astra.social.application.service;

import com.astra.social.application.port.in.CheckFriendshipQuery;
import com.astra.social.application.port.in.ExportFriendshipsQuery;
import com.astra.social.application.port.in.FriendshipResponse;
import com.astra.social.application.port.in.GetFriendIdsQuery;
import com.astra.social.application.port.in.ListFriendshipsQuery;
import com.astra.social.application.port.out.ListFriendshipsPort;
import com.astra.social.application.port.out.LoadFriendshipPort;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.social.application.port.out.LoadUserSummariesPort.UserSummary;
import com.astra.social.domain.Friendship;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class FriendshipQueryService implements ListFriendshipsQuery, GetFriendIdsQuery, CheckFriendshipQuery,
        ExportFriendshipsQuery {

    private final ListFriendshipsPort listFriendshipsPort;
    private final LoadFriendshipPort loadFriendshipPort;
    private final LoadUserSummariesPort loadUserSummariesPort;

    FriendshipQueryService(ListFriendshipsPort listFriendshipsPort, LoadFriendshipPort loadFriendshipPort,
                           LoadUserSummariesPort loadUserSummariesPort) {
        this.listFriendshipsPort = listFriendshipsPort;
        this.loadFriendshipPort = loadFriendshipPort;
        this.loadUserSummariesPort = loadUserSummariesPort;
    }

    @Override
    public List<FriendshipResponse> friends(UUID userId) {
        return FriendshipResponses.of(listFriendshipsPort.listAccepted(userId), userId, loadUserSummariesPort);
    }

    @Override
    public List<FriendshipResponse> pendingRequests(UUID userId) {
        return FriendshipResponses.of(listFriendshipsPort.listPending(userId), userId, loadUserSummariesPort);
    }

    @Override
    public List<UUID> friendIdsOf(UUID userId) {
        return listFriendshipsPort.listAccepted(userId).stream()
                .map(friendship -> friendship.otherUserId(userId))
                .toList();
    }

    @Override
    public boolean areFriends(UUID userId, UUID otherUserId) {
        return loadFriendshipPort.loadBetween(userId, otherUserId)
                .map(Friendship::isAccepted)
                .orElse(false);
    }

    @Override
    public List<FriendshipExport> export(UUID userId) {
        List<Friendship> friendships = listFriendshipsPort.listAll(userId);
        if (friendships.isEmpty()) {
            return List.of();
        }
        Map<UUID, UserSummary> users = loadUserSummariesPort.loadByIds(
                friendships.stream().map(f -> f.otherUserId(userId)).toList());
        return friendships.stream()
                .map(f -> {
                    UUID otherId = f.otherUserId(userId);
                    UserSummary other = users.get(otherId);
                    return new FriendshipExport(otherId, other == null ? null : other.handle(),
                            f.getStatus().name(), f.isRequester(userId), f.getCreatedAt());
                })
                .toList();
    }
}
