package com.astra.chat.application.service;

import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;

final class Access {

    private Access() {
    }

    static void requireFriends(CheckFriendshipPort checkFriendshipPort, UUID a, UUID b) {
        if (!checkFriendshipPort.areFriends(a, b)) {
            throw new NotFoundException("Conversa não encontrada");
        }
    }

    static void requireMember(CheckGroupMemberPort checkGroupMemberPort, UUID groupId, UUID userId) {
        if (!checkGroupMemberPort.isMember(groupId, userId)) {
            throw new NotFoundException("Grupo não encontrado");
        }
    }
}
