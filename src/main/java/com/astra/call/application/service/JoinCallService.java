package com.astra.call.application.service;

import com.astra.call.application.port.in.JoinCallUseCase;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class JoinCallService implements JoinCallUseCase {

    private final CallBoardActions actions;
    private final PortsCallDirectory directory;

    JoinCallService(CallBoardActions actions, PortsCallDirectory directory) {
        this.actions = actions;
        this.directory = directory;
    }

    @Override
    public void join(UUID userId, String sessionId, UUID callId, String clientId) {
        actions.apply(board -> board.join(userId, sessionId, callId, clientId, directory));
    }
}
