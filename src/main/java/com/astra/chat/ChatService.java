package com.astra.chat;

import com.astra.chat.dto.AttachmentData;
import com.astra.chat.dto.ConversationSummary;
import com.astra.chat.dto.MessageResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.social.FriendshipService;
import com.astra.user.User;
import com.astra.user.UserRepository;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final UserRepository userRepository;
    private final FriendshipService friendshipService;
    private final CurrentUserProvider currentUserProvider;

    public ChatService(MessageRepository messageRepository, MessageAttachmentRepository messageAttachmentRepository,
            UserRepository userRepository, FriendshipService friendshipService,
            CurrentUserProvider currentUserProvider) {
        this.messageRepository = messageRepository;
        this.messageAttachmentRepository = messageAttachmentRepository;
        this.userRepository = userRepository;
        this.friendshipService = friendshipService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public MessageResponse send(UUID recipientId, String content, UUID replyToMessageId) {
        UUID me = currentUserProvider.currentUserId();
        if (content == null || content.isBlank()) {
            throw new ConflictException("Mensagem precisa ter texto");
        }
        requireFriends(me, recipientId);
        UUID validReplyTo = validatedReplyTarget(me, recipientId, replyToMessageId);
        Message created = new Message(me, recipientId, content);
        created.setReplyToMessageId(validReplyTo);
        return toDto(messageRepository.saveAndFlush(created));
    }

    @Transactional
    public MessageResponse sendImage(UUID recipientId, String caption, UUID replyToMessageId, MultipartFile file) {
        UUID me = currentUserProvider.currentUserId();
        requireFriends(me, recipientId);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ConflictException("Arquivo precisa ser uma imagem");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ConflictException("Imagem precisa ter até 2MB");
        }

        UUID validReplyTo = validatedReplyTarget(me, recipientId, replyToMessageId);
        String trimmedCaption = caption == null || caption.isBlank() ? null : caption;
        Message created = new Message(me, recipientId, trimmedCaption);
        created.setReplyToMessageId(validReplyTo);
        Message saved = messageRepository.saveAndFlush(created);

        try {
            MessageAttachment attachment = messageAttachmentRepository
                    .save(new MessageAttachment(saved.getId(), contentType, file.getBytes()));
            return toDto(saved, attachment.getId(), replyPreviewFor(validReplyTo));
        } catch (IOException ex) {
            throw new ConflictException("Não foi possível ler o arquivo");
        }
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> history(UUID friendId, int limit) {
        UUID me = currentUserProvider.currentUserId();
        requireFriends(me, friendId);
        List<Message> messages = messageRepository.findConversation(me, friendId, PageRequest.of(0, limit));

        List<UUID> messageIds = messages.stream().map(Message::getId).toList();
        Map<UUID, UUID> attachmentByMessage = messageAttachmentRepository.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.toMap(MessageAttachment::getMessageId, MessageAttachment::getId));

        List<UUID> replyIds = messages.stream()
                .map(Message::getReplyToMessageId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<UUID, MessageResponse.ReplyPreview> replyPreviewById = replyIds.isEmpty()
                ? Map.of()
                : messageRepository.findAllById(replyIds).stream()
                        .collect(Collectors.toMap(Message::getId, this::toReplyPreview));

        return messages.stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .map(m -> toDto(m, attachmentByMessage.get(m.getId()),
                        m.getReplyToMessageId() == null ? null : replyPreviewById.get(m.getReplyToMessageId())))
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
    public AttachmentData attachment(UUID messageId) {
        UUID me = currentUserProvider.currentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        if (!message.getSenderId().equals(me) && !message.getRecipientId().equals(me)) {
            throw new NotFoundException("Não encontrado");
        }
        MessageAttachment attachment = messageAttachmentRepository.findByMessageId(messageId)
                .orElseThrow(() -> new NotFoundException("Não encontrado"));
        return new AttachmentData(attachment.getData(), attachment.getContentType());
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
                    boolean friendAdmin = user != null && User.ROLE_ADMIN.equals(user.getRole());
                    long unread = unreadByFriend.getOrDefault(friendId, 0L);
                    MessageRepository.LastMessageView last = lastByFriend.get(friendId);
                    return last != null
                            ? new ConversationSummary(friendId, friendName, friendBio, friendAdmin, lastMessageText(last),
                                    last.getCreatedAt().atOffset(ZoneOffset.UTC), unread)
                            : new ConversationSummary(friendId, friendName, friendBio, friendAdmin, null, null, unread);
                })
                .sorted(Comparator.comparing(ConversationSummary::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private static String lastMessageText(MessageRepository.LastMessageView last) {
        return last.getContent() != null ? last.getContent() : "📷 Foto";
    }

    private void requireFriends(UUID a, UUID b) {
        if (!friendshipService.areFriends(a, b)) {
            throw new NotFoundException("Conversa não encontrada");
        }
    }

    private UUID validatedReplyTarget(UUID me, UUID otherId, UUID replyToMessageId) {
        if (replyToMessageId == null) {
            return null;
        }
        Message target = messageRepository.findById(replyToMessageId)
                .orElseThrow(() -> new NotFoundException("Mensagem original não encontrada"));
        boolean sameConversation = (target.getSenderId().equals(me) && target.getRecipientId().equals(otherId))
                || (target.getSenderId().equals(otherId) && target.getRecipientId().equals(me));
        if (!sameConversation) {
            throw new ConflictException("Mensagem não pertence a esta conversa");
        }
        return replyToMessageId;
    }

    private MessageResponse.ReplyPreview replyPreviewFor(UUID replyToMessageId) {
        if (replyToMessageId == null) {
            return null;
        }
        return messageRepository.findById(replyToMessageId).map(this::toReplyPreview).orElse(null);
    }

    private MessageResponse.ReplyPreview toReplyPreview(Message message) {
        String content = message.getContent();
        String preview;
        if (content == null || content.isBlank()) {
            preview = "📷 Foto";
        } else if (content.length() > 80) {
            preview = content.substring(0, 80) + "…";
        } else {
            preview = content;
        }
        return new MessageResponse.ReplyPreview(message.getId(), message.getSenderId(), preview);
    }

    private MessageResponse toDto(Message message) {
        UUID attachmentId = messageAttachmentRepository.findByMessageId(message.getId())
                .map(MessageAttachment::getId)
                .orElse(null);
        return toDto(message, attachmentId, replyPreviewFor(message.getReplyToMessageId()));
    }

    private MessageResponse toDto(Message message, UUID attachmentId, MessageResponse.ReplyPreview replyTo) {
        return new MessageResponse(message.getId(), message.getSenderId(), message.getRecipientId(),
                message.getContent(), message.getCreatedAt(), message.isRead(), attachmentId, replyTo);
    }
}
