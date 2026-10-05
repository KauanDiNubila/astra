package com.astra.call.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SignalRequest(
        @NotNull UUID callId,
        @NotBlank @Size(max = 64) String toClient,
        @NotBlank @Size(max = 16) String type,
        @NotBlank @Size(max = 60000) String data
) {
}
