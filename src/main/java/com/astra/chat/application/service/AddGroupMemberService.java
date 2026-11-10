package com.astra.chat.application.service;

import com.astra.chat.application.port.in.AddGroupMemberUseCase;
import com.astra.chat.application.port.out.AddGroupMemberPort;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.LoadGroupPort;
import com.astra.chat.domain.ChatGroup;
import com.astra.chat.domain.NotAFriendException;
import com.astra.shared.exception.NotFoundException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddGroupMemberService implements AddGroupMemberUseCase {

    private final LoadGroupPort loadGroupPort;
    private final CheckFriendshipPort checkFriendshipPort;
    private final AddGroupMemberPort addGroupMemberPort;
    private final Clock clock;

    AddGroupMemberService(LoadGroupPort loadGroupPort, CheckFriendshipPort checkFriendshipPort,
                          AddGroupMemberPort addGroupMemberPort, Clock clock) {
        this.loadGroupPort = loadGroupPort;
        this.checkFriendshipPort = checkFriendshipPort;
        this.addGroupMemberPort = addGroupMemberPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void add(UUID userId, UUID groupId, UUID newMemberId) {
        ChatGroup group = loadGroupPort.load(groupId)
                .filter(g -> g.isMember(userId))
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (!checkFriendshipPort.areFriends(userId, newMemberId)) {
            throw new NotAFriendException();
        }
        addGroupMemberPort.add(groupId, group.addMember(userId, newMemberId, OffsetDateTime.now(clock)));
    }
}
