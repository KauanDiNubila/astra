package com.astra.chat.application.service;

import com.astra.chat.application.port.in.ConversationSummary;
import com.astra.chat.application.port.in.CountUnreadQuery;
import com.astra.chat.application.port.in.ExportMessagesQuery;
import com.astra.chat.application.port.in.GetAttachmentQuery;
import com.astra.chat.application.port.in.ImageResponse;
import com.astra.chat.application.port.in.ListConversationsQuery;
import com.astra.chat.application.port.in.ListMessagesQuery;
import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.CountUnreadPort;
import com.astra.chat.application.port.out.ListMessagesPort;
import com.astra.chat.application.port.out.LoadAttachmentPort;
import com.astra.chat.application.port.out.LoadFriendIdsPort;
import com.astra.chat.application.port.out.LoadGithubProfilesPort;
import com.astra.chat.application.port.out.LoadGithubProfilesPort.GithubProfile;
import com.astra.chat.application.port.out.LoadLastDirectMessagesPort;
import com.astra.chat.application.port.out.LoadLastDirectMessagesPort.LastMessage;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.application.port.out.LoadUserCardsPort;
import com.astra.chat.application.port.out.LoadUserCardsPort.UserCard;
import com.astra.chat.domain.ChatImage;
import com.astra.chat.domain.Message;
import com.astra.shared.ExportRow;
import com.astra.shared.exception.NotFoundException;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ChatQueryService implements ListConversationsQuery, ListMessagesQuery, CountUnreadQuery, GetAttachmentQuery,
        ExportMessagesQuery {

    private static final Comparator<MessageResponse> PINNED_FIRST =
            Comparator.comparing(MessageResponse::pinnedAt).reversed();

    private final LoadFriendIdsPort loadFriendIdsPort;
    private final CheckFriendshipPort checkFriendshipPort;
    private final CheckGroupMemberPort checkGroupMemberPort;
    private final LoadUserCardsPort loadUserCardsPort;
    private final LoadGithubProfilesPort loadGithubProfilesPort;
    private final CountUnreadPort countUnreadPort;
    private final LoadLastDirectMessagesPort loadLastDirectMessagesPort;
    private final ListMessagesPort listMessagesPort;
    private final LoadMessagePort loadMessagePort;
    private final LoadAttachmentPort loadAttachmentPort;
    private final MessageResponses messageResponses;

    ChatQueryService(LoadFriendIdsPort loadFriendIdsPort, CheckFriendshipPort checkFriendshipPort,
                     CheckGroupMemberPort checkGroupMemberPort, LoadUserCardsPort loadUserCardsPort,
                     LoadGithubProfilesPort loadGithubProfilesPort, CountUnreadPort countUnreadPort,
                     LoadLastDirectMessagesPort loadLastDirectMessagesPort, ListMessagesPort listMessagesPort,
                     LoadMessagePort loadMessagePort, LoadAttachmentPort loadAttachmentPort,
                     MessageResponses messageResponses) {
        this.loadFriendIdsPort = loadFriendIdsPort;
        this.checkFriendshipPort = checkFriendshipPort;
        this.checkGroupMemberPort = checkGroupMemberPort;
        this.loadUserCardsPort = loadUserCardsPort;
        this.loadGithubProfilesPort = loadGithubProfilesPort;
        this.countUnreadPort = countUnreadPort;
        this.loadLastDirectMessagesPort = loadLastDirectMessagesPort;
        this.listMessagesPort = listMessagesPort;
        this.loadMessagePort = loadMessagePort;
        this.loadAttachmentPort = loadAttachmentPort;
        this.messageResponses = messageResponses;
    }

    @Override
    public List<ConversationSummary> conversations(UUID userId) {
        List<UUID> friendIds = loadFriendIdsPort.friendIdsOf(userId);
        if (friendIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, UserCard> cards = loadUserCardsPort.cards(friendIds);
        Map<UUID, GithubProfile> github = loadGithubProfilesPort.visibleToFriends(friendIds);
        Map<UUID, Long> unread = countUnreadPort.directBySender(userId, friendIds);
        Map<UUID, LastMessage> last = loadLastDirectMessagesPort.lastWith(userId, friendIds);
        return friendIds.stream()
                .map(friendId -> summary(friendId, cards.get(friendId), github.get(friendId), last.get(friendId),
                        unread.getOrDefault(friendId, 0L)))
                .sorted(Comparator.comparing(ConversationSummary::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private static ConversationSummary summary(UUID friendId, UserCard card, GithubProfile github, LastMessage last,
                                               long unread) {
        return new ConversationSummary(friendId,
                card != null ? card.name() : "",
                card != null ? card.tag() : "",
                card != null ? card.bio() : null,
                card != null && card.staff(),
                github != null ? github.login() : null,
                github != null ? github.avatarUrl() : null,
                last == null ? null : last.content() != null ? last.content() : Message.PHOTO_PLACEHOLDER,
                last == null ? null : last.createdAt(),
                unread);
    }

    @Override
    public List<MessageResponse> directHistory(UUID userId, UUID friendId, int limit) {
        Access.requireFriends(checkFriendshipPort, userId, friendId);
        return messageResponses.many(listMessagesPort.directConversation(userId, friendId, limit));
    }

    @Override
    public List<MessageResponse> groupHistory(UUID userId, UUID groupId, int limit) {
        Access.requireMember(checkGroupMemberPort, groupId, userId);
        return messageResponses.many(listMessagesPort.group(groupId, limit));
    }

    @Override
    public List<MessageResponse> directPinned(UUID userId, UUID friendId) {
        Access.requireFriends(checkFriendshipPort, userId, friendId);
        return messageResponses.many(listMessagesPort.pinnedDirect(userId, friendId)).stream()
                .sorted(PINNED_FIRST).toList();
    }

    @Override
    public List<MessageResponse> groupPinned(UUID userId, UUID groupId) {
        Access.requireMember(checkGroupMemberPort, groupId, userId);
        return messageResponses.many(listMessagesPort.pinnedInGroup(groupId)).stream()
                .sorted(PINNED_FIRST).toList();
    }

    @Override
    public long direct(UUID userId) {
        return countUnreadPort.directTo(userId);
    }

    @Override
    public long groups(UUID userId) {
        List<UUID> groupIds = checkGroupMemberPort.groupIdsOf(userId);
        if (groupIds.isEmpty()) {
            return 0;
        }
        return countUnreadPort.inGroups(userId, groupIds).values().stream().mapToLong(Long::longValue).sum();
    }

    @Override
    public ImageResponse attachment(UUID userId, UUID messageId) {
        Message message = loadMessagePort.load(messageId)
                .filter(m -> m.involves(userId)
                        || (m.getGroupId() != null && checkGroupMemberPort.isMember(m.getGroupId(), userId)))
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        ChatImage image = loadAttachmentPort.loadFor(message.getId())
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        return new ImageResponse(image.bytes(), image.contentType());
    }

    @Override
    public List<Map<String, Object>> exportMessages(UUID userId) {
        List<Message> messages = listMessagesPort.forExport(userId);
        Map<UUID, ChatImage> images = messages.isEmpty() ? Map.of()
                : loadAttachmentPort.loadForAll(messages.stream().map(Message::getId).toList());
        return messages.stream()
                .map(message -> {
                    boolean mine = message.getSenderId().equals(userId);
                    ChatImage image = images.get(message.getId());
                    Map<String, Object> imageRow = null;
                    if (image != null) {
                        imageRow = ExportRow.of("contentType", image.contentType());
                        if (mine) {
                            imageRow.put("base64", Base64.getEncoder().encodeToString(image.bytes()));
                        }
                    }
                    return ExportRow.of("id", message.getId(), "sentByMe", mine,
                            "senderId", message.getSenderId(), "recipientId", message.getRecipientId(),
                            "groupId", message.getGroupId(),
                            "content", message.getContent(),
                            "replyToMessageId", message.getReplyToMessageId(),
                            "pinnedAt", message.getPinnedAt(),
                            "image", imageRow,
                            "createdAt", message.getCreatedAt(), "readAt", message.getReadAt());
                })
                .toList();
    }
}
