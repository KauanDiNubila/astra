package com.astra.call.application.service;

import com.astra.call.application.port.out.CheckFriendshipPort;
import com.astra.call.application.port.out.LoadGroupInfoPort;
import com.astra.call.application.port.out.LoadUserNamePort;
import com.astra.call.domain.CallDirectory;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PortsCallDirectory implements CallDirectory {

    private final CheckFriendshipPort checkFriendshipPort;
    private final LoadGroupInfoPort loadGroupInfoPort;
    private final LoadUserNamePort loadUserNamePort;

    PortsCallDirectory(CheckFriendshipPort checkFriendshipPort, LoadGroupInfoPort loadGroupInfoPort,
                       LoadUserNamePort loadUserNamePort) {
        this.checkFriendshipPort = checkFriendshipPort;
        this.loadGroupInfoPort = loadGroupInfoPort;
        this.loadUserNamePort = loadUserNamePort;
    }

    @Override
    public boolean areFriends(UUID a, UUID b) {
        return checkFriendshipPort.areFriends(a, b);
    }

    @Override
    public List<UUID> groupMembers(UUID groupId) {
        return loadGroupInfoPort.memberIds(groupId);
    }

    @Override
    public String groupName(UUID groupId) {
        return loadGroupInfoPort.name(groupId);
    }

    @Override
    public String nameOf(UUID userId) {
        return loadUserNamePort.nameOf(userId);
    }
}
