package com.astra.call.application.service;

import com.astra.call.application.port.in.StartCallCommand;
import com.astra.call.application.port.in.StartCallUseCase;
import com.astra.call.domain.CallBoard;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
class StartCallService implements StartCallUseCase {

    static final Duration RING_TIMEOUT = Duration.ofSeconds(45);

    private final CallBoardActions actions;
    private final PortsCallDirectory directory;

    StartCallService(CallBoardActions actions, PortsCallDirectory directory) {
        this.actions = actions;
        this.directory = directory;
    }

    @Override
    public void start(StartCallCommand command) {
        CallBoard.StartResult result = actions.apply(board -> board.start(command.userId(), command.sessionId(),
                command.clientId(), command.targetUserId(), command.groupId(), directory), CallBoard.StartResult::notices);
        result.ringingCallId().ifPresent(callId -> actions.applyLater(RING_TIMEOUT, board -> board.expire(callId)));
    }
}
