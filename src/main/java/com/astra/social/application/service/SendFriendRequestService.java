package com.astra.social.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.social.application.port.in.FriendshipResponse;
import com.astra.social.application.port.in.SendFriendRequestCommand;
import com.astra.social.application.port.in.SendFriendRequestUseCase;
import com.astra.social.application.port.out.CreateFriendshipPort;
import com.astra.social.application.port.out.FindUserByHandlePort;
import com.astra.social.application.port.out.LoadFriendshipPort;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.social.application.port.out.UpdateFriendshipStatusPort;
import com.astra.social.domain.Friendship;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SendFriendRequestService implements SendFriendRequestUseCase {

    private final FindUserByHandlePort findUserByHandlePort;
    private final LoadFriendshipPort loadFriendshipPort;
    private final CreateFriendshipPort createFriendshipPort;
    private final UpdateFriendshipStatusPort updateFriendshipStatusPort;
    private final LoadUserSummariesPort loadUserSummariesPort;

    SendFriendRequestService(FindUserByHandlePort findUserByHandlePort, LoadFriendshipPort loadFriendshipPort,
                             CreateFriendshipPort createFriendshipPort,
                             UpdateFriendshipStatusPort updateFriendshipStatusPort,
                             LoadUserSummariesPort loadUserSummariesPort) {
        this.findUserByHandlePort = findUserByHandlePort;
        this.loadFriendshipPort = loadFriendshipPort;
        this.createFriendshipPort = createFriendshipPort;
        this.updateFriendshipStatusPort = updateFriendshipStatusPort;
        this.loadUserSummariesPort = loadUserSummariesPort;
    }

    @Override
    @Transactional
    public FriendshipResponse send(SendFriendRequestCommand command) {
        UUID me = command.userId();
        UUID target = findUserByHandlePort.findIdByHandle(command.name(), command.tag())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Optional<Friendship> existing = loadFriendshipPort.loadBetween(me, target);
        Friendship friendship;
        if (existing.isPresent()) {
            friendship = existing.get();
            friendship.receiveRequestFrom(me);
            updateFriendshipStatusPort.updateStatus(friendship);
        } else {
            friendship = createFriendshipPort.create(Friendship.request(me, target));
        }
        return FriendshipResponses.of(friendship, me, loadUserSummariesPort);
    }
}
