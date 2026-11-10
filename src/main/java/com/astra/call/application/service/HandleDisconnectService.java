package com.astra.call.application.service;

import com.astra.call.application.port.in.HandleDisconnectUseCase;
import com.astra.call.domain.StaleMember;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
class HandleDisconnectService implements HandleDisconnectUseCase {

    static final Duration DISCONNECT_GRACE = Duration.ofSeconds(30);

    private final CallBoardActions actions;

    HandleDisconnectService(CallBoardActions actions) {
        this.actions = actions;
    }

    @Override
    public void disconnected(String sessionId) {
        for (StaleMember stale : actions.read(board -> board.membersOnSession(sessionId))) {
            actions.applyLater(DISCONNECT_GRACE, board -> board.dropIfStale(stale));
        }
    }
}
