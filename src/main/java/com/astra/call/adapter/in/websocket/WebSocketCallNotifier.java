package com.astra.call.adapter.in.websocket;

import com.astra.call.application.port.out.NotifyCallPort;
import com.astra.call.domain.CallNotice;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
class WebSocketCallNotifier implements NotifyCallPort {

    private final SimpMessagingTemplate messagingTemplate;

    WebSocketCallNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void send(List<CallNotice> notices) {
        for (CallNotice notice : notices) {
            messagingTemplate.convertAndSendToUser(notice.to().toString(), "/queue/call",
                    CallEventMessage.from(notice.event()));
        }
    }
}
