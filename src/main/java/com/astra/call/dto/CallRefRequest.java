package com.astra.call.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CallRefRequest(
        @NotNull UUID callId,
        @Size(max = 64) String clientId
) {
}
