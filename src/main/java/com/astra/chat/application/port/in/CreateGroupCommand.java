package com.astra.chat.application.port.in;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateGroupCommand(UUID userId, String name, List<UUID> memberIds) {

    public static final int NAME_MAX_LENGTH = 60;

    public CreateGroupCommand {
        Objects.requireNonNull(userId);
        if (name == null || name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("name must not be blank and have at most " + NAME_MAX_LENGTH
                    + " characters");
        }
        if (memberIds == null || memberIds.isEmpty()) {
            throw new IllegalArgumentException("memberIds must not be empty");
        }
        memberIds = List.copyOf(memberIds);
    }
}
