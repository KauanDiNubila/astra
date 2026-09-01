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

    // Exatamente um de recipientId/groupId é preenchido (ver constraint
    // chk_message_target) — mensagem de grupo reaproveita esta mesma
    // tabela em vez de duplicar anexo/reply/criptografia numa paralela.
    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "group_id")
    private UUID groupId;

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

    public static Message forGroup(UUID senderId, UUID groupId, String content) {
        Message message = new Message();
        message.senderId = senderId;
        message.groupId = groupId;
        message.content = content;
        return message;
    }

    public boolean isRead() {
        return readAt != null;
    }
}
