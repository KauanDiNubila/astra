package com.astra.call.adapter.in.websocket;

import com.astra.call.application.port.in.HandleDisconnectUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
class DisconnectListener {

    private final HandleDisconnectUseCase handleDisconnectUseCase;

    DisconnectListener(HandleDisconnectUseCase handleDisconnectUseCase) {
        this.handleDisconnectUseCase = handleDisconnectUseCase;
    }

    @EventListener
    void onDisconnect(SessionDisconnectEvent event) {
        handleDisconnectUseCase.disconnected(event.getSessionId());
    }
}
