package com.astra.chat.domain;

import java.util.Objects;
import java.util.UUID;

public record Destination(UUID recipientId, UUID groupId) {

    public Destination {
        if ((recipientId == null) == (groupId == null)) {
            throw new IllegalArgumentException("a message goes to exactly one recipient or one group");
        }
    }

    public static Destination direct(UUID recipientId) {
        return new Destination(Objects.requireNonNull(recipientId), null);
    }

    public static Destination group(UUID groupId) {
        return new Destination(null, Objects.requireNonNull(groupId));
    }

    public boolean isGroup() {
        return groupId != null;
    }
}
