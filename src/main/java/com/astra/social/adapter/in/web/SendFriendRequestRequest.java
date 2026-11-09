package com.astra.social.adapter.in.web;

import com.astra.social.application.port.in.SendFriendRequestCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

record SendFriendRequestRequest(
        @NotBlank @Pattern(regexp = SendFriendRequestCommand.HANDLE_PATTERN, message = "Use o formato nome#0000")
        String handle
) {

    SendFriendRequestCommand toCommand(UUID userId) {
        return new SendFriendRequestCommand(userId, handle);
    }
}
