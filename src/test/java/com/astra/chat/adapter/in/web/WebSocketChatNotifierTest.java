package com.astra.chat.adapter.in.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.astra.chat.application.port.in.MessageResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class WebSocketChatNotifierTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BETO = UUID.randomUUID();
    private static final MessageResponse MESSAGE = new MessageResponse(UUID.randomUUID(), ANA, BETO, null, "oi",
            OffsetDateTime.parse("2026-11-10T12:00:00Z"), false, null, null, null, null);

    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final WebSocketChatNotifier notifier = new WebSocketChatNotifier(template);

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void dentroDeTransacaoSoAvisaDepoisQueGrava() {
        TransactionSynchronizationManager.initSynchronization();

        notifier.messageSent(MESSAGE, List.of(ANA, BETO));
        verifyNoInteractions(template);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(template).convertAndSendToUser(ANA.toString(), "/queue/messages", MESSAGE);
        verify(template).convertAndSendToUser(BETO.toString(), "/queue/messages", MESSAGE);
    }

    @Test
    void foraDeTransacaoAvisaNaHoraNoCanalDeAtualizacoes() {
        notifier.messageChanged(MESSAGE, List.of(BETO));

        verify(template).convertAndSendToUser(BETO.toString(), "/queue/message-updates", MESSAGE);
    }
}
