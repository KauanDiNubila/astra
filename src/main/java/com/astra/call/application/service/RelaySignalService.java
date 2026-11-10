package com.astra.call.application.service;

import com.astra.call.application.port.in.RelaySignalCommand;
import com.astra.call.application.port.in.RelaySignalUseCase;
import java.time.Clock;
import org.springframework.stereotype.Service;

@Service
class RelaySignalService implements RelaySignalUseCase {

    private final CallBoardActions actions;
    private final Clock clock;

    RelaySignalService(CallBoardActions actions, Clock clock) {
        this.actions = actions;
        this.clock = clock;
    }

    @Override
    public void relay(RelaySignalCommand command) {
        actions.apply(board -> board.relay(command.userId(), command.callId(), command.toClient(), command.type(),
                command.data(), clock.millis()));
    }
}
