package com.astra.chat.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Message {

    public static final int MAX_PINNED_PER_CONVERSATION = 3;
    public static final String PHOTO_PLACEHOLDER = "📷 Foto";
    private static final int PREVIEW_LENGTH = 80;

    private final UUID id;
    private final UUID senderId;
    private final Destination destination;
    private final String content;
    private UUID replyToMessageId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime readAt;
    private OffsetDateTime pinnedAt;
    private UUID pinnedBy;

    private Message(UUID id, UUID senderId, Destination destination, String content, UUID replyToMessageId,
                    OffsetDateTime createdAt, OffsetDateTime readAt, OffsetDateTime pinnedAt, UUID pinnedBy) {
        this.id = id;
        this.senderId = Objects.requireNonNull(senderId);
        this.destination = Objects.requireNonNull(destination);
        this.content = content;
        this.replyToMessageId = replyToMessageId;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.pinnedAt = pinnedAt;
        this.pinnedBy = pinnedBy;
    }

    public static Message text(UUID senderId, Destination destination, String content) {
        if (content == null || content.isBlank()) {
            throw new EmptyMessageException();
        }
        return new Message(null, senderId, destination, content, null, null, null, null, null);
    }

    public static Message photo(UUID senderId, Destination destination, String caption) {
        String text = caption == null || caption.isBlank() ? null : caption;
        return new Message(null, senderId, destination, text, null, null, null, null, null);
    }

    public static Message withId(UUID id, UUID senderId, Destination destination, String content,
                                 UUID replyToMessageId, OffsetDateTime createdAt, OffsetDateTime readAt,
                                 OffsetDateTime pinnedAt, UUID pinnedBy) {
        return new Message(Objects.requireNonNull(id), senderId, destination, content, replyToMessageId, createdAt,
                readAt, pinnedAt, pinnedBy);
    }

    public UUID getRecipientId() {
        return destination.recipientId();
    }

    public UUID getGroupId() {
        return destination.groupId();
    }

    public boolean isRead() {
        return readAt != null;
    }

    public boolean isPinned() {
        return pinnedAt != null;
    }

    public boolean involves(UUID userId) {
        return senderId.equals(userId) || userId.equals(getRecipientId());
    }

    public UUID otherParticipant(UUID userId) {
        return senderId.equals(userId) ? getRecipientId() : senderId;
    }

    public boolean isBetween(UUID a, UUID b) {
        return !destination.isGroup()
                && ((senderId.equals(a) && b.equals(getRecipientId())) || (senderId.equals(b) && a.equals(getRecipientId())));
    }

    public boolean isInSameConversationAs(Message other) {
        return destination.isGroup()
                ? destination.groupId().equals(other.getGroupId())
                : other.isBetween(senderId, getRecipientId());
    }

    public void replyTo(Message original) {
        if (!isInSameConversationAs(original)) {
            throw new ReplyOutsideConversationException();
        }
        this.replyToMessageId = original.getId();
    }

    public void pin(UUID userId, OffsetDateTime now, int alreadyPinned) {
        if (isPinned()) {
            return;
        }
        if (alreadyPinned >= MAX_PINNED_PER_CONVERSATION) {
            throw new PinLimitReachedException(MAX_PINNED_PER_CONVERSATION);
        }
        this.pinnedAt = now;
        this.pinnedBy = userId;
    }

    public void unpin() {
        this.pinnedAt = null;
        this.pinnedBy = null;
    }

    public String summaryText() {
        return content != null ? content : PHOTO_PLACEHOLDER;
    }

    public String preview() {
        if (content == null || content.isBlank()) {
            return PHOTO_PLACEHOLDER;
        }
        return content.length() > PREVIEW_LENGTH ? content.substring(0, PREVIEW_LENGTH) + "…" : content;
    }
}
