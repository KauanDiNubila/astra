package com.astra.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "chat_group_member")
@IdClass(ChatGroupMemberId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatGroupMember {

    @Id
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    // baseline da contagem de não lidas — começa em "agora" pra quem entra
    // no grupo não herdar o histórico inteiro como não lido.
    @Column(name = "last_read_at", nullable = false)
    private OffsetDateTime lastReadAt;

    public ChatGroupMember(UUID groupId, UUID userId) {
        this.groupId = groupId;
        this.userId = userId;
        this.lastReadAt = OffsetDateTime.now();
    }
}
