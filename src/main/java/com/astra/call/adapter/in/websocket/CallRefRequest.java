package com.astra.call.adapter.in.websocket;

import com.astra.call.application.port.in.StartCallCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CallRefRequest(@NotNull UUID callId, @Size(max = StartCallCommand.CLIENT_ID_MAX_LENGTH) String clientId) {
}
