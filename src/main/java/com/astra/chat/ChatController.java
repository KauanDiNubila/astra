package com.astra.chat;

import com.astra.chat.dto.AttachmentData;
import com.astra.chat.dto.ConversationSummary;
import com.astra.chat.dto.MessageResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/conversations")
    public List<ConversationSummary> conversations() {
        return chatService.conversations();
    }

    @GetMapping("/{friendId}/messages")
    public List<MessageResponse> history(@PathVariable UUID friendId, @RequestParam(defaultValue = "50") int limit) {
        return chatService.history(friendId, Math.min(limit, 200));
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

    @PostMapping("/{friendId}/messages/image")
    public MessageResponse sendImage(@PathVariable UUID friendId,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) UUID replyToMessageId,
            @RequestParam("file") MultipartFile file) {
        MessageResponse response = chatService.sendImage(friendId, caption, replyToMessageId, file);
        messagingTemplate.convertAndSendToUser(response.senderId().toString(), "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(friendId.toString(), "/queue/messages", response);
        return response;
    }

    @GetMapping("/messages/{messageId}/attachment")
    public ResponseEntity<byte[]> attachment(@PathVariable UUID messageId) {
        AttachmentData data = chatService.attachment(messageId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .contentType(MediaType.parseMediaType(data.contentType()))
                .body(data.bytes());
    }
}
