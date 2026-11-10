package com.astra.call.adapter.in.websocket;

import com.astra.call.application.port.in.RelaySignalCommand;
import com.astra.call.application.port.in.StartCallCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record SignalRequest(
        @NotNull UUID callId,
        @NotBlank @Size(max = StartCallCommand.CLIENT_ID_MAX_LENGTH) String toClient,
        @NotBlank @Size(max = 16) String type,
        @NotBlank @Size(max = RelaySignalCommand.DATA_MAX_LENGTH) String data
) {
}
