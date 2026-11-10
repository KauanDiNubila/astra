package com.astra.chat.adapter.in.web;

import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.out.NotifyChatPort;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
class WebSocketChatNotifier implements NotifyChatPort {

    private final SimpMessagingTemplate messagingTemplate;

    WebSocketChatNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void messageSent(MessageResponse message, List<UUID> audience) {
        afterCommit(() -> send(message, audience, "/queue/messages"));
    }

    @Override
    public void messageChanged(MessageResponse message, List<UUID> audience) {
        afterCommit(() -> send(message, audience, "/queue/message-updates"));
    }

    private void send(MessageResponse message, List<UUID> audience, String destination) {
        for (UUID userId : audience) {
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, message);
        }
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
