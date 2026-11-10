package com.astra.call.application.service;

import com.astra.call.application.port.in.DeclineCallUseCase;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class DeclineCallService implements DeclineCallUseCase {

    private final CallBoardActions actions;

    DeclineCallService(CallBoardActions actions) {
        this.actions = actions;
    }

    @Override
    public void decline(UUID userId, UUID callId) {
        actions.apply(board -> board.decline(userId, callId));
    }
}
