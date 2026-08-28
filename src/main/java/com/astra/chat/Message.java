package com.astra.chat;

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
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    // texto cifrado (AES-256-GCM, ver ChatEncryptionService) — bem maior
    // que o limite de 2000 caracteres do texto original por causa do
    // base64 + IV/tag, por isso "text" em vez de um length fixo.
    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "reply_to_message_id")
    private UUID replyToMessageId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    public Message(UUID senderId, UUID recipientId, String content) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
    }

    public boolean isRead() {
        return readAt != null;
    }
}
