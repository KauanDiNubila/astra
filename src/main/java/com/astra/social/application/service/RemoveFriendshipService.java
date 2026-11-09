package com.astra.social.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.social.application.port.in.RemoveFriendshipUseCase;
import com.astra.social.application.port.out.DeleteFriendshipPort;
import com.astra.social.application.port.out.LoadFriendshipPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RemoveFriendshipService implements RemoveFriendshipUseCase {

    private final LoadFriendshipPort loadFriendshipPort;
    private final DeleteFriendshipPort deleteFriendshipPort;

    RemoveFriendshipService(LoadFriendshipPort loadFriendshipPort, DeleteFriendshipPort deleteFriendshipPort) {
        this.loadFriendshipPort = loadFriendshipPort;
        this.deleteFriendshipPort = deleteFriendshipPort;
    }

    @Override
    @Transactional
    public void remove(UUID userId, UUID friendshipId) {
        loadFriendshipPort.load(friendshipId)
                .filter(friendship -> friendship.involves(userId))
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        deleteFriendshipPort.delete(friendshipId);
    }
}
