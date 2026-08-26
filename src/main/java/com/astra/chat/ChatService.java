package com.astra.chat;

import com.astra.chat.dto.ConversationSummary;
import com.astra.chat.dto.MessageResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import com.astra.social.FriendshipService;
import com.astra.user.UserRepository;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private final CurrentUserProvider currentUserProvider;

    public ChatService(MessageRepository messageRepository, UserRepository userRepository,
            FriendshipService friendshipService, CurrentUserProvider currentUserProvider) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public MessageResponse send(UUID recipientId, String content) {
        UUID me = currentUserProvider.currentUserId();
        requireFriends(me, recipientId);
        Message created = new Message(me, recipientId, content);
        return toDto(messageRepository.saveAndFlush(created));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> history(UUID friendId, int limit) {
        UUID me = currentUserProvider.currentUserId();
        requireFriends(me, friendId);
        List<Message> messages = messageRepository.findConversation(me, friendId, PageRequest.of(0, limit));
        return messages.stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void markRead(UUID friendId) {
        UUID me = currentUserProvider.currentUserId();
        requireFriends(me, friendId);
        messageRepository.markRead(friendId, me, OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        UUID me = currentUserProvider.currentUserId();
        return messageRepository.countByRecipientIdAndReadAtIsNull(me);
    }

    @Transactional(readOnly = true)
    public List<ConversationSummary> conversations() {
        UUID me = currentUserProvider.currentUserId();
        List<UUID> friendIds = friendshipService.friendIdsOf(me);
        if (friendIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, UserRepository.NameBioView> usersById = userRepository.findNameBioByIdIn(friendIds).stream()
                .collect(Collectors.toMap(UserRepository.NameBioView::getId, v -> v));
        Map<UUID, Long> unreadByFriend = messageRepository.unreadCountsFor(me, friendIds).stream()
                .collect(Collectors.toMap(MessageRepository.UnreadBySender::getFriendId,
                        MessageRepository.UnreadBySender::getUnread));
        Map<UUID, MessageRepository.LastMessageView> lastByFriend = messageRepository.lastMessagesFor(me, friendIds)
                .stream()
                .collect(Collectors.toMap(v -> v.getSenderId().equals(me) ? v.getRecipientId() : v.getSenderId(),
                        v -> v));

        return friendIds.stream()
                .map(friendId -> {
                    UserRepository.NameBioView user = usersById.get(friendId);
                    String friendName = user != null ? user.getName() : "";
                    String friendBio = user != null ? user.getBio() : null;
                    long unread = unreadByFriend.getOrDefault(friendId, 0L);
                    MessageRepository.LastMessageView last = lastByFriend.get(friendId);
                    return last != null
                            ? new ConversationSummary(friendId, friendName, friendBio, last.getContent(), last.getCreatedAt(), unread)
                            : new ConversationSummary(friendId, friendName, friendBio, null, null, unread);
                })
                .sorted(Comparator.comparing(ConversationSummary::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private void requireFriends(UUID a, UUID b) {
        if (!friendshipService.areFriends(a, b)) {
            throw new NotFoundException("Conversa não encontrada");
        }
    }

    private MessageResponse toDto(Message message) {
        return new MessageResponse(message.getId(), message.getSenderId(), message.getRecipientId(),
                message.getContent(), message.getCreatedAt(), message.isRead());
    }
}
