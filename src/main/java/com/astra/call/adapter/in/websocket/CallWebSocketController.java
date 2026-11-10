package com.astra.call.adapter.in.websocket;

import com.astra.call.application.port.in.DeclineCallUseCase;
import com.astra.call.application.port.in.JoinCallUseCase;
import com.astra.call.application.port.in.LeaveCallUseCase;
import com.astra.call.application.port.in.RelaySignalCommand;
import com.astra.call.application.port.in.RelaySignalUseCase;
import com.astra.call.application.port.in.StartCallCommand;
import com.astra.call.application.port.in.StartCallUseCase;
import com.astra.call.domain.CallEvent;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
class CallWebSocketController {

    private final StartCallUseCase startCallUseCase;
    private final JoinCallUseCase joinCallUseCase;
    private final DeclineCallUseCase declineCallUseCase;
    private final LeaveCallUseCase leaveCallUseCase;
    private final RelaySignalUseCase relaySignalUseCase;

    CallWebSocketController(StartCallUseCase startCallUseCase, JoinCallUseCase joinCallUseCase,
                            DeclineCallUseCase declineCallUseCase, LeaveCallUseCase leaveCallUseCase,
                            RelaySignalUseCase relaySignalUseCase) {
        this.startCallUseCase = startCallUseCase;
        this.joinCallUseCase = joinCallUseCase;
        this.declineCallUseCase = declineCallUseCase;
        this.leaveCallUseCase = leaveCallUseCase;
        this.relaySignalUseCase = relaySignalUseCase;
    }

    @MessageMapping("/call.start")
    void start(@Payload @Valid StartCallRequest request, Principal principal, SimpMessageHeaderAccessor headers) {
        startCallUseCase.start(new StartCallCommand(userId(principal), headers.getSessionId(),
                request.targetUserId(), request.groupId(), request.clientId()));
    }

    @MessageMapping("/call.join")
    void join(@Payload @Valid CallRefRequest request, Principal principal, SimpMessageHeaderAccessor headers) {
        if (request.clientId() == null || request.clientId().isBlank()) {
            throw new ConflictException("Identificador da aba ausente");
        }
        joinCallUseCase.join(userId(principal), headers.getSessionId(), request.callId(), request.clientId());
    }

    @MessageMapping("/call.decline")
    void decline(@Payload @Valid CallRefRequest request, Principal principal) {
        declineCallUseCase.decline(userId(principal), request.callId());
    }

    @MessageMapping("/call.leave")
    void leave(@Payload @Valid CallRefRequest request, Principal principal) {
        leaveCallUseCase.leave(userId(principal), request.callId(), request.clientId());
    }

    @MessageMapping("/call.signal")
    void signal(@Payload @Valid SignalRequest request, Principal principal) {
        relaySignalUseCase.relay(new RelaySignalCommand(userId(principal), request.callId(), request.toClient(),
                request.type(), request.data()));
    }

    @MessageExceptionHandler({ConflictException.class, NotFoundException.class})
    @SendToUser(destinations = "/queue/call", broadcast = false)
    CallEventMessage onDomainError(RuntimeException ex) {
        return CallEventMessage.from(CallEvent.error(ex.getMessage()));
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/call", broadcast = false)
    CallEventMessage onInvalid(Exception ex) {
        return CallEventMessage.from(CallEvent.error("Requisição de call inválida"));
    }

    private static UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
