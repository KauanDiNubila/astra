package com.astra.chat;

import com.astra.chat.dto.ConversationSummary;
import com.astra.chat.dto.MessageResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import com.astra.social.FriendshipService;
import com.astra.user.User;
import com.astra.user.UserRepository;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
        return friendshipService.friendIdsOf(me).stream()
                .map(friendId -> toConversationSummary(me, friendId))
                .sorted(Comparator.comparing(ConversationSummary::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private ConversationSummary toConversationSummary(UUID me, UUID friendId) {
        Optional<User> friend = userRepository.findById(friendId);
        String friendName = friend.map(User::getName).orElse("");
        String friendBio = friend.map(User::getBio).orElse(null);
        long unread = messageRepository.countBySenderIdAndRecipientIdAndReadAtIsNull(friendId, me);
        return messageRepository.findConversation(me, friendId, PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(last -> new ConversationSummary(friendId, friendName, friendBio, last.getContent(), last.getCreatedAt(), unread))
                .orElseGet(() -> new ConversationSummary(friendId, friendName, friendBio, null, null, unread));
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
