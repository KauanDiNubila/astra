package com.astra.chat;

import com.astra.chat.dto.MessageResponse;
import com.astra.chat.dto.SendMessageRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(@Payload @Valid SendMessageRequest request, Principal principal) {
        UUID me = UUID.fromString(principal.getName());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(me, null, List.of()));
        try {
            MessageResponse response = chatService.send(request.recipientId(), request.content());
            messagingTemplate.convertAndSendToUser(me.toString(), "/queue/messages", response);
            messagingTemplate.convertAndSendToUser(request.recipientId().toString(), "/queue/messages", response);
        } catch (RuntimeException ex) {
            messagingTemplate.convertAndSendToUser(me.toString(), "/queue/errors", ex.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
