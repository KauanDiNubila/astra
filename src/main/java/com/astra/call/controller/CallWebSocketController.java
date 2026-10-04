package com.astra.call.controller;

import com.astra.call.dto.CallEvent;
import com.astra.call.dto.CallRefRequest;
import com.astra.call.dto.SignalRequest;
import com.astra.call.dto.StartCallRequest;
import com.astra.call.service.CallService;
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
public class CallWebSocketController {

    private final CallService callService;

    public CallWebSocketController(CallService callService) {
        this.callService = callService;
    }

    @MessageMapping("/call.start")
    public void start(@Payload @Valid StartCallRequest request, Principal principal,
            SimpMessageHeaderAccessor headers) {
        callService.start(userId(principal), headers.getSessionId(), request);
    }

    @MessageMapping("/call.join")
    public void join(@Payload @Valid CallRefRequest request, Principal principal, SimpMessageHeaderAccessor headers) {
        if (request.clientId() == null || request.clientId().isBlank()) {
            throw new ConflictException("Identificador da aba ausente");
        }
        callService.join(userId(principal), headers.getSessionId(), request.callId(), request.clientId());
    }

    @MessageMapping("/call.decline")
    public void decline(@Payload @Valid CallRefRequest request, Principal principal) {
        callService.decline(userId(principal), request.callId());
    }

    @MessageMapping("/call.leave")
    public void leave(@Payload @Valid CallRefRequest request, Principal principal) {
        callService.leave(userId(principal), request.callId(), request.clientId());
    }

    @MessageMapping("/call.signal")
    public void signal(@Payload @Valid SignalRequest request, Principal principal) {
        callService.relay(userId(principal), request);
    }

    @MessageExceptionHandler({ConflictException.class, NotFoundException.class})
    @SendToUser(destinations = "/queue/call", broadcast = false)
    public CallEvent onDomainError(RuntimeException ex) {
        return CallEvent.error(ex.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/call", broadcast = false)
    public CallEvent onInvalid(Exception ex) {
        return CallEvent.error("Requisição de call inválida");
    }

    private UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
