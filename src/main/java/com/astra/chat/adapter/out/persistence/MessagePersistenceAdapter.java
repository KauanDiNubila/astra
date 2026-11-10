package com.astra.chat.adapter.out.persistence;

import com.astra.chat.application.port.out.CountUnreadPort;
import com.astra.chat.application.port.out.CreateMessagePort;
import com.astra.chat.application.port.out.ListMessagesPort;
import com.astra.chat.application.port.out.LoadAttachmentPort;
import com.astra.chat.application.port.out.LoadLastDirectMessagesPort;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.application.port.out.MarkConversationReadPort;
import com.astra.chat.application.port.out.UpdateMessagePinPort;
import com.astra.chat.domain.ChatImage;
import com.astra.chat.domain.Destination;
import com.astra.chat.domain.Message;
import com.astra.shared.crypto.EncryptionService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class MessagePersistenceAdapter implements CreateMessagePort, LoadMessagePort, UpdateMessagePinPort, ListMessagesPort,
        LoadLastDirectMessagesPort, MarkConversationReadPort, CountUnreadPort, LoadAttachmentPort {

    private final SpringDataMessageRepository messages;
    private final SpringDataAttachmentRepository attachments;
    private final SpringDataGroupMemberRepository members;
    private final EncryptionService encryption;

    MessagePersistenceAdapter(SpringDataMessageRepository messages, SpringDataAttachmentRepository attachments,
                              SpringDataGroupMemberRepository members, EncryptionService encryption) {
        this.messages = messages;
        this.attachments = attachments;
        this.members = members;
        this.encryption = encryption;
    }

    private Message toDomain(MessageJpaEntity e) {
        Destination destination = e.getGroupId() != null
                ? Destination.group(e.getGroupId())
                : Destination.direct(e.getRecipientId());
        return Message.withId(e.getId(), e.getSenderId(), destination, encryption.decrypt(e.getContent()),
                e.getReplyToMessageId(), e.getCreatedAt(), e.getReadAt(), e.getPinnedAt(), e.getPinnedBy());
    }

    private List<Message> toDomain(List<MessageJpaEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public Message create(Message message, ChatImage attachment) {
        MessageJpaEntity saved = messages.saveAndFlush(new MessageJpaEntity(message.getSenderId(),
                message.getRecipientId(), message.getGroupId(), encryption.encrypt(message.getContent()),
                message.getReplyToMessageId()));
        if (attachment != null) {
            attachments.save(new AttachmentJpaEntity(saved.getId(), attachment.contentType(),
                    encryption.encryptBytes(attachment.bytes())));
        }
        return toDomain(saved);
    }

    @Override
    public Optional<Message> load(UUID messageId) {
        return messages.findById(messageId).map(this::toDomain);
    }

    @Override
    public List<Message> loadAll(Collection<UUID> messageIds) {
        return toDomain(messages.findAllById(messageIds));
    }

    @Override
    public void updatePin(Message message) {
        MessageJpaEntity entity = messages.findById(message.getId()).orElseThrow();
        entity.setPinnedAt(message.getPinnedAt());
        entity.setPinnedBy(message.getPinnedBy());
        messages.flush();
    }

    @Override
    public List<Message> directConversation(UUID a, UUID b, int limit) {
        return toDomain(messages.findConversation(a, b, PageRequest.of(0, limit)));
    }

    @Override
    public List<Message> group(UUID groupId, int limit) {
        return toDomain(messages.findByGroupIdOrderByCreatedAtDesc(groupId, PageRequest.of(0, limit)));
    }

    @Override
    public List<Message> pinnedDirect(UUID a, UUID b) {
        return toDomain(messages.findPinnedInConversation(a, b));
    }

    @Override
    public List<Message> pinnedInGroup(UUID groupId) {
        return toDomain(messages.findByGroupIdAndPinnedAtIsNotNullOrderByPinnedAtDesc(groupId));
    }

    @Override
    public Optional<Message> lastInGroup(UUID groupId) {
        return messages.findFirstByGroupIdOrderByCreatedAtDesc(groupId).map(this::toDomain);
    }

    @Override
    public List<Message> forExport(UUID userId) {
        return toDomain(messages.findAllForExport(userId));
    }

    @Override
    public Map<UUID, LastMessage> lastWith(UUID userId, Collection<UUID> friendIds) {
        return messages.lastMessagesFor(userId, friendIds).stream()
                .collect(Collectors.toMap(v -> v.getSenderId().equals(userId) ? v.getRecipientId() : v.getSenderId(),
                        v -> new LastMessage(encryption.decrypt(v.getContent()),
                                v.getCreatedAt().atOffset(ZoneOffset.UTC))));
    }

    @Override
    public void markRead(UUID senderId, UUID recipientId, OffsetDateTime now) {
        messages.markRead(senderId, recipientId, now);
    }

    @Override
    public long directTo(UUID userId) {
        return messages.countByRecipientIdAndReadAtIsNull(userId);
    }

    @Override
    public Map<UUID, Long> directBySender(UUID userId, Collection<UUID> friendIds) {
        return messages.unreadCountsFor(userId, friendIds).stream()
                .collect(Collectors.toMap(SpringDataMessageRepository.UnreadBySender::getFriendId,
                        SpringDataMessageRepository.UnreadBySender::getUnread));
    }

    @Override
    public Map<UUID, Long> inGroups(UUID userId, Collection<UUID> groupIds) {
        return members.unreadCountsFor(userId, groupIds).stream()
                .collect(Collectors.toMap(SpringDataGroupMemberRepository.UnreadByGroup::getGroupId,
                        SpringDataGroupMemberRepository.UnreadByGroup::getUnread));
    }

    @Override
    public Map<UUID, UUID> attachmentIdsFor(Collection<UUID> messageIds) {
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        return attachments.findIdsByMessageIdIn(messageIds).stream()
                .collect(Collectors.toMap(SpringDataAttachmentRepository.AttachmentIdView::getMessageId,
                        SpringDataAttachmentRepository.AttachmentIdView::getId));
    }

    @Override
    public Optional<ChatImage> loadFor(UUID messageId) {
        return attachments.findByMessageId(messageId).map(this::toImage);
    }

    @Override
    public Map<UUID, ChatImage> loadForAll(Collection<UUID> messageIds) {
        return attachments.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.toMap(AttachmentJpaEntity::getMessageId, this::toImage));
    }

    private ChatImage toImage(AttachmentJpaEntity entity) {
        return new ChatImage(entity.getContentType(), encryption.decryptBytes(entity.getData()));
    }
}
