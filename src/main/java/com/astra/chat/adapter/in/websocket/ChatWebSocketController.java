package com.astra.chat.adapter.in.websocket;

import com.astra.chat.application.port.in.SendDirectMessageCommand;
import com.astra.chat.application.port.in.SendDirectMessageUseCase;
import com.astra.chat.application.port.in.SendGroupMessageCommand;
import com.astra.chat.application.port.in.SendGroupMessageUseCase;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
class ChatWebSocketController {

    private final SendDirectMessageUseCase sendDirectMessageUseCase;
    private final SendGroupMessageUseCase sendGroupMessageUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    ChatWebSocketController(SendDirectMessageUseCase sendDirectMessageUseCase,
                            SendGroupMessageUseCase sendGroupMessageUseCase, SimpMessagingTemplate messagingTemplate) {
        this.sendDirectMessageUseCase = sendDirectMessageUseCase;
        this.sendGroupMessageUseCase = sendGroupMessageUseCase;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    void send(@Payload @Valid SendMessageRequest request, Principal principal) {
        UUID me = UUID.fromString(principal.getName());
        reportingErrorsTo(me, () -> sendDirectMessageUseCase.send(new SendDirectMessageCommand(me,
                request.recipientId(), request.content(), request.replyToMessageId(), null)));
    }

    @MessageMapping("/chat.sendGroup")
    void sendGroup(@Payload @Valid SendGroupMessageRequest request, Principal principal) {
        UUID me = UUID.fromString(principal.getName());
        reportingErrorsTo(me, () -> sendGroupMessageUseCase.send(new SendGroupMessageCommand(me, request.groupId(),
                request.content(), request.replyToMessageId(), null)));
    }

    private void reportingErrorsTo(UUID userId, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/errors", ex.getMessage());
        }
    }
}
