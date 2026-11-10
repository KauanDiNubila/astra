package com.astra.chat.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record GroupMember(UUID userId, OffsetDateTime joinedAt, OffsetDateTime lastReadAt) {

    public GroupMember {
        Objects.requireNonNull(userId);
    }

    static GroupMember joining(UUID userId, OffsetDateTime now) {
        return new GroupMember(userId, now, now);
    }
}
