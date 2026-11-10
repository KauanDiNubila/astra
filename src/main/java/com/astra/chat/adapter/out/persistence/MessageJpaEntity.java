package com.astra.chat.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class MessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "reply_to_message_id")
    private UUID replyToMessageId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "pinned_at")
    private OffsetDateTime pinnedAt;

    @Column(name = "pinned_by")
    private UUID pinnedBy;

    MessageJpaEntity(UUID senderId, UUID recipientId, UUID groupId, String content, UUID replyToMessageId) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.groupId = groupId;
        this.content = content;
        this.replyToMessageId = replyToMessageId;
    }
}
