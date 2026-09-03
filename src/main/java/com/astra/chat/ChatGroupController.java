package com.astra.chat;

import com.astra.chat.dto.AddGroupMemberRequest;
import com.astra.chat.dto.CreateGroupRequest;
import com.astra.chat.dto.GroupConversationSummary;
import com.astra.chat.dto.GroupMemberResponse;
import com.astra.chat.dto.MessageResponse;
import com.astra.user.dto.AvatarData;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat/groups")
public class ChatGroupController {

    private final ChatGroupService chatGroupService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatGroupController(ChatGroupService chatGroupService, ChatService chatService,
            SimpMessagingTemplate messagingTemplate) {
        this.chatGroupService = chatGroupService;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupConversationSummary create(@Valid @RequestBody CreateGroupRequest request) {
        return chatGroupService.create(request);
    }

    @GetMapping
    public List<GroupConversationSummary> conversations() {
        return chatGroupService.conversations();
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        return chatService.unreadCountGroups();
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> members(@PathVariable UUID groupId) {
        return chatGroupService.members(groupId);
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMember(@PathVariable UUID groupId, @Valid @RequestBody AddGroupMemberRequest request) {
        chatGroupService.addMember(groupId, request.userId());
    }

    @PostMapping("/{groupId}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAvatar(@PathVariable UUID groupId, @RequestParam("file") MultipartFile file) {
        chatGroupService.updateAvatar(groupId, file);
    }

    @GetMapping("/{groupId}/avatar")
    public ResponseEntity<byte[]> avatar(@PathVariable UUID groupId) {
        AvatarData avatar = chatGroupService.avatar(groupId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .contentType(MediaType.parseMediaType(avatar.contentType()))
                .body(avatar.bytes());
    }

    @GetMapping("/{groupId}/messages")
    public List<MessageResponse> history(@PathVariable UUID groupId, @RequestParam(defaultValue = "50") int limit) {
        return chatService.historyForGroup(groupId, Math.min(limit, 200));
    }

    @PostMapping("/{groupId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable UUID groupId) {
        chatService.markReadGroup(groupId);
    }

    @PostMapping("/{groupId}/messages/image")
    public MessageResponse sendImage(@PathVariable UUID groupId,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) UUID replyToMessageId,
            @RequestParam("file") MultipartFile file) {
        MessageResponse response = chatService.sendGroupImage(groupId, caption, replyToMessageId, file);
        for (UUID memberId : chatGroupService.memberIds(groupId)) {
            messagingTemplate.convertAndSendToUser(memberId.toString(), "/queue/messages", response);
        }
        return response;
    }
}
