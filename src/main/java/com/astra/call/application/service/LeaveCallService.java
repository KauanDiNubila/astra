package com.astra.call.application.service;

import com.astra.call.application.port.in.LeaveCallUseCase;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class LeaveCallService implements LeaveCallUseCase {

    private final CallBoardActions actions;

    LeaveCallService(CallBoardActions actions) {
        this.actions = actions;
    }

    @Override
    public void leave(UUID userId, UUID callId, String clientId) {
        actions.apply(board -> board.leave(userId, callId, clientId));
    }
}
