package com.astra.chat;

import com.astra.chat.dto.ConversationSummary;
import com.astra.chat.dto.MessageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public List<ConversationSummary> conversations() {
        return chatService.conversations();
    }

    @GetMapping("/{friendId}/messages")
    public List<MessageResponse> history(@PathVariable UUID friendId, @RequestParam(defaultValue = "50") int limit) {
        return chatService.history(friendId, limit);
    }

    @PostMapping("/{friendId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable UUID friendId) {
        chatService.markRead(friendId);
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        return chatService.unreadCount();
    }
}
