package com.astra.social.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.social.application.port.in.AcceptFriendRequestUseCase;
import com.astra.social.application.port.in.FriendshipResponse;
import com.astra.social.application.port.out.LoadFriendshipPort;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.social.application.port.out.UpdateFriendshipStatusPort;
import com.astra.social.domain.Friendship;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AcceptFriendRequestService implements AcceptFriendRequestUseCase {

    private final LoadFriendshipPort loadFriendshipPort;
    private final UpdateFriendshipStatusPort updateFriendshipStatusPort;
    private final LoadUserSummariesPort loadUserSummariesPort;

    AcceptFriendRequestService(LoadFriendshipPort loadFriendshipPort,
                               UpdateFriendshipStatusPort updateFriendshipStatusPort,
                               LoadUserSummariesPort loadUserSummariesPort) {
        this.loadFriendshipPort = loadFriendshipPort;
        this.updateFriendshipStatusPort = updateFriendshipStatusPort;
        this.loadUserSummariesPort = loadUserSummariesPort;
    }

    @Override
    @Transactional
    public FriendshipResponse accept(UUID userId, UUID friendshipId) {
        Friendship friendship = loadFriendshipPort.load(friendshipId)
                .filter(f -> f.isAddressee(userId))
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        friendship.accept();
        updateFriendshipStatusPort.updateStatus(friendship);
        return FriendshipResponses.of(friendship, userId, loadUserSummariesPort);
    }
}
