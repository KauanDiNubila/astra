package com.astra.chat.adapter.in.web;

import com.astra.chat.application.port.in.CreateGroupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

record CreateGroupRequest(
        @NotBlank @Size(max = CreateGroupCommand.NAME_MAX_LENGTH) String name,
        @NotEmpty List<UUID> memberIds
) {

    CreateGroupCommand toCommand(UUID userId) {
        return new CreateGroupCommand(userId, name, memberIds);
    }
}
