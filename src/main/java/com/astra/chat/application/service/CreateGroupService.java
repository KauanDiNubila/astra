package com.astra.chat.application.service;

import com.astra.chat.application.port.in.CreateGroupCommand;
import com.astra.chat.application.port.in.CreateGroupUseCase;
import com.astra.chat.application.port.in.GroupConversationSummary;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.CreateGroupPort;
import com.astra.chat.application.port.out.LoadUserCardsPort;
import com.astra.chat.domain.ChatGroup;
import com.astra.chat.domain.NotAFriendException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateGroupService implements CreateGroupUseCase {

    private final CheckFriendshipPort checkFriendshipPort;
    private final CreateGroupPort createGroupPort;
    private final LoadUserCardsPort loadUserCardsPort;
    private final Clock clock;

    CreateGroupService(CheckFriendshipPort checkFriendshipPort, CreateGroupPort createGroupPort,
                       LoadUserCardsPort loadUserCardsPort, Clock clock) {
        this.checkFriendshipPort = checkFriendshipPort;
        this.createGroupPort = createGroupPort;
        this.loadUserCardsPort = loadUserCardsPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public GroupConversationSummary create(CreateGroupCommand command) {
        for (UUID memberId : command.memberIds()) {
            if (!memberId.equals(command.userId()) && !checkFriendshipPort.areFriends(command.userId(), memberId)) {
                throw new NotAFriendException();
            }
        }
        ChatGroup group = createGroupPort.create(ChatGroup.create(command.name(), command.userId(),
                command.memberIds(), OffsetDateTime.now(clock)));
        List<UUID> memberIds = group.memberIds();
        MemberNames names = MemberNames.of(loadUserCardsPort, memberIds);
        return new GroupConversationSummary(group.getId(), group.getName(),
                memberIds.stream().map(names::nameOf).toList(), null, null, 0L);
    }
}
