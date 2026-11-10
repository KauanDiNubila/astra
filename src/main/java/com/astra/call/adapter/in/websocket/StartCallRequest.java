package com.astra.call.adapter.in.websocket;

import com.astra.call.application.port.in.StartCallCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record StartCallRequest(
        UUID targetUserId,
        UUID groupId,
        @NotBlank @Size(max = StartCallCommand.CLIENT_ID_MAX_LENGTH) String clientId
) {
}
