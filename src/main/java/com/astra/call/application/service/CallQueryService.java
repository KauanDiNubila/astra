package com.astra.call.application.service;

import com.astra.call.application.port.in.ActiveCallResponse;
import com.astra.call.application.port.in.ListActiveCallsQuery;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class CallQueryService implements ListActiveCallsQuery {

    private final CallBoardActions actions;

    CallQueryService(CallBoardActions actions) {
        this.actions = actions;
    }

    @Override
    public List<ActiveCallResponse> activeFor(UUID userId) {
        return actions.read(board -> board.activeFor(userId)).stream()
                .map(a -> new ActiveCallResponse(a.callId(), a.groupId(), a.participantCount()))
                .toList();
    }
}
