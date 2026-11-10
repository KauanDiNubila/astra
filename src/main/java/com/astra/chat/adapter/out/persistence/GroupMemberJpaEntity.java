package com.astra.chat.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_group_member")
@IdClass(GroupMemberJpaEntity.Key.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class GroupMemberJpaEntity {

    @Id
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "last_read_at", nullable = false)
    private OffsetDateTime lastReadAt;

    GroupMemberJpaEntity(UUID groupId, UUID userId, OffsetDateTime joinedAt, OffsetDateTime lastReadAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.lastReadAt = lastReadAt;
    }

    record Key(UUID groupId, UUID userId) implements Serializable {

        Key() {
            this(null, null);
        }
    }
}
