package com.astra.chat.adapter.in.web;

import com.astra.chat.application.port.in.AddGroupMemberUseCase;
import com.astra.chat.application.port.in.CountUnreadQuery;
import com.astra.chat.application.port.in.CreateGroupUseCase;
import com.astra.chat.application.port.in.GetGroupPhotoQuery;
import com.astra.chat.application.port.in.GroupConversationSummary;
import com.astra.chat.application.port.in.GroupMemberResponse;
import com.astra.chat.application.port.in.ListGroupConversationsQuery;
import com.astra.chat.application.port.in.ListGroupMembersQuery;
import com.astra.chat.application.port.in.ListMessagesQuery;
import com.astra.chat.application.port.in.MarkGroupReadUseCase;
import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.in.SendGroupMessageCommand;
import com.astra.chat.application.port.in.SendGroupMessageUseCase;
import com.astra.chat.application.port.in.UpdateGroupPhotoUseCase;
import com.astra.shared.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
class ChatGroupController {

    private static final int MAX_HISTORY = 200;

    private final CreateGroupUseCase createGroupUseCase;
    private final AddGroupMemberUseCase addGroupMemberUseCase;
    private final UpdateGroupPhotoUseCase updateGroupPhotoUseCase;
    private final SendGroupMessageUseCase sendGroupMessageUseCase;
    private final MarkGroupReadUseCase markGroupReadUseCase;
    private final ListGroupConversationsQuery listGroupConversationsQuery;
    private final ListGroupMembersQuery listGroupMembersQuery;
    private final GetGroupPhotoQuery getGroupPhotoQuery;
    private final ListMessagesQuery listMessagesQuery;
    private final CountUnreadQuery countUnreadQuery;
    private final CurrentUserProvider currentUserProvider;

    ChatGroupController(CreateGroupUseCase createGroupUseCase, AddGroupMemberUseCase addGroupMemberUseCase,
                        UpdateGroupPhotoUseCase updateGroupPhotoUseCase,
                        SendGroupMessageUseCase sendGroupMessageUseCase, MarkGroupReadUseCase markGroupReadUseCase,
                        ListGroupConversationsQuery listGroupConversationsQuery,
                        ListGroupMembersQuery listGroupMembersQuery, GetGroupPhotoQuery getGroupPhotoQuery,
                        ListMessagesQuery listMessagesQuery, CountUnreadQuery countUnreadQuery,
                        CurrentUserProvider currentUserProvider) {
        this.createGroupUseCase = createGroupUseCase;
        this.addGroupMemberUseCase = addGroupMemberUseCase;
        this.updateGroupPhotoUseCase = updateGroupPhotoUseCase;
        this.sendGroupMessageUseCase = sendGroupMessageUseCase;
        this.markGroupReadUseCase = markGroupReadUseCase;
        this.listGroupConversationsQuery = listGroupConversationsQuery;
        this.listGroupMembersQuery = listGroupMembersQuery;
        this.getGroupPhotoQuery = getGroupPhotoQuery;
        this.listMessagesQuery = listMessagesQuery;
        this.countUnreadQuery = countUnreadQuery;
        this.currentUserProvider = currentUserProvider;
    }

    private UUID me() {
        return currentUserProvider.currentUserId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    GroupConversationSummary create(@Valid @RequestBody CreateGroupRequest request) {
        return createGroupUseCase.create(request.toCommand(me()));
    }

    @GetMapping
    List<GroupConversationSummary> conversations() {
        return listGroupConversationsQuery.groups(me());
    }

    @GetMapping("/unread-count")
    long unreadCount() {
        return countUnreadQuery.groups(me());
    }

    @GetMapping("/{groupId}/members")
    List<GroupMemberResponse> members(@PathVariable UUID groupId) {
        return listGroupMembersQuery.members(me(), groupId);
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void addMember(@PathVariable UUID groupId, @Valid @RequestBody AddGroupMemberRequest request) {
        addGroupMemberUseCase.add(me(), groupId, request.userId());
    }

    @PostMapping("/{groupId}/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateAvatar(@PathVariable UUID groupId, @RequestParam("file") MultipartFile file) {
        updateGroupPhotoUseCase.update(me(), groupId, Uploads.read(file));
    }

    @GetMapping("/{groupId}/avatar")
    ResponseEntity<byte[]> avatar(@PathVariable UUID groupId) {
        return Uploads.image(getGroupPhotoQuery.photo(me(), groupId));
    }

    @GetMapping("/{groupId}/messages")
    List<MessageResponse> history(@PathVariable UUID groupId, @RequestParam(defaultValue = "50") int limit) {
        return listMessagesQuery.groupHistory(me(), groupId, Math.min(limit, MAX_HISTORY));
    }

    @GetMapping("/{groupId}/pins")
    List<MessageResponse> pins(@PathVariable UUID groupId) {
        return listMessagesQuery.groupPinned(me(), groupId);
    }

    @PostMapping("/{groupId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void markRead(@PathVariable UUID groupId) {
        markGroupReadUseCase.markRead(me(), groupId);
    }

    @PostMapping("/{groupId}/messages/image")
    MessageResponse sendImage(@PathVariable UUID groupId, @RequestParam(required = false) String caption,
                              @RequestParam(required = false) UUID replyToMessageId,
                              @RequestParam("file") MultipartFile file) {
        return sendGroupMessageUseCase.send(new SendGroupMessageCommand(me(), groupId, caption, replyToMessageId,
                Uploads.read(file)));
    }
}
