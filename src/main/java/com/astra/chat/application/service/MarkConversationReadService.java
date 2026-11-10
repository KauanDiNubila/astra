package com.astra.chat.application.service;

import com.astra.chat.application.port.in.MarkConversationReadUseCase;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.MarkConversationReadPort;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MarkConversationReadService implements MarkConversationReadUseCase {

    private final CheckFriendshipPort checkFriendshipPort;
    private final MarkConversationReadPort markConversationReadPort;
    private final Clock clock;

    MarkConversationReadService(CheckFriendshipPort checkFriendshipPort,
                                MarkConversationReadPort markConversationReadPort, Clock clock) {
        this.checkFriendshipPort = checkFriendshipPort;
        this.markConversationReadPort = markConversationReadPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void markRead(UUID userId, UUID friendId) {
        Access.requireFriends(checkFriendshipPort, userId, friendId);
        markConversationReadPort.markRead(friendId, userId, OffsetDateTime.now(clock));
    }
}
