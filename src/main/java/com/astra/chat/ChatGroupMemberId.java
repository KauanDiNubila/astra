package com.astra.chat;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ChatGroupMemberId implements Serializable {

    private UUID groupId;
    private UUID userId;

    public ChatGroupMemberId() {
    }

    public ChatGroupMemberId(UUID groupId, UUID userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatGroupMemberId that)) {
            return false;
        }
        return Objects.equals(groupId, that.groupId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}
