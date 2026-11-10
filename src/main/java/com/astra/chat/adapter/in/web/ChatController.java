package com.astra.chat.adapter.in.web;

import com.astra.chat.application.port.in.ConversationSummary;
import com.astra.chat.application.port.in.CountUnreadQuery;
import com.astra.chat.application.port.in.GetAttachmentQuery;
import com.astra.chat.application.port.in.ListConversationsQuery;
import com.astra.chat.application.port.in.ListMessagesQuery;
import com.astra.chat.application.port.in.MarkConversationReadUseCase;
import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.in.PinMessageUseCase;
import com.astra.chat.application.port.in.SendDirectMessageCommand;
import com.astra.chat.application.port.in.SendDirectMessageUseCase;
import com.astra.shared.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
class ChatController {

    private static final int MAX_HISTORY = 200;

    private final ListConversationsQuery listConversationsQuery;
    private final ListMessagesQuery listMessagesQuery;
    private final CountUnreadQuery countUnreadQuery;
    private final GetAttachmentQuery getAttachmentQuery;
    private final SendDirectMessageUseCase sendDirectMessageUseCase;
    private final PinMessageUseCase pinMessageUseCase;
    private final MarkConversationReadUseCase markConversationReadUseCase;
    private final CurrentUserProvider currentUserProvider;

    ChatController(ListConversationsQuery listConversationsQuery, ListMessagesQuery listMessagesQuery,
                   CountUnreadQuery countUnreadQuery, GetAttachmentQuery getAttachmentQuery,
                   SendDirectMessageUseCase sendDirectMessageUseCase, PinMessageUseCase pinMessageUseCase,
                   MarkConversationReadUseCase markConversationReadUseCase, CurrentUserProvider currentUserProvider) {
        this.listConversationsQuery = listConversationsQuery;
        this.listMessagesQuery = listMessagesQuery;
        this.countUnreadQuery = countUnreadQuery;
        this.getAttachmentQuery = getAttachmentQuery;
        this.sendDirectMessageUseCase = sendDirectMessageUseCase;
        this.pinMessageUseCase = pinMessageUseCase;
        this.markConversationReadUseCase = markConversationReadUseCase;
        this.currentUserProvider = currentUserProvider;
    }

    private UUID me() {
        return currentUserProvider.currentUserId();
    }

    @GetMapping("/conversations")
    List<ConversationSummary> conversations() {
        return listConversationsQuery.conversations(me());
    }

    @GetMapping("/{friendId}/messages")
    List<MessageResponse> history(@PathVariable UUID friendId, @RequestParam(defaultValue = "50") int limit) {
        return listMessagesQuery.directHistory(me(), friendId, Math.min(limit, MAX_HISTORY));
    }

    @GetMapping("/{friendId}/pins")
    List<MessageResponse> pins(@PathVariable UUID friendId) {
        return listMessagesQuery.directPinned(me(), friendId);
    }

    @PutMapping("/messages/{messageId}/pin")
    MessageResponse pin(@PathVariable UUID messageId) {
        return pinMessageUseCase.setPinned(me(), messageId, true);
    }

    @DeleteMapping("/messages/{messageId}/pin")
    MessageResponse unpin(@PathVariable UUID messageId) {
        return pinMessageUseCase.setPinned(me(), messageId, false);
    }

    @PostMapping("/{friendId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void markRead(@PathVariable UUID friendId) {
        markConversationReadUseCase.markRead(me(), friendId);
    }

    @GetMapping("/unread-count")
    long unreadCount() {
        return countUnreadQuery.direct(me());
    }

    @PostMapping("/{friendId}/messages/image")
    MessageResponse sendImage(@PathVariable UUID friendId, @RequestParam(required = false) String caption,
                              @RequestParam(required = false) UUID replyToMessageId,
                              @RequestParam("file") MultipartFile file) {
        return sendDirectMessageUseCase.send(new SendDirectMessageCommand(me(), friendId, caption, replyToMessageId,
                Uploads.read(file)));
    }

    @GetMapping("/messages/{messageId}/attachment")
    ResponseEntity<byte[]> attachment(@PathVariable UUID messageId) {
        return Uploads.image(getAttachmentQuery.attachment(me(), messageId));
    }
}
