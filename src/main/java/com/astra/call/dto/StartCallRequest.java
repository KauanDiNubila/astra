package com.astra.call.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StartCallRequest(
        UUID targetUserId,
        UUID groupId,
        @NotBlank @Size(max = 64) String clientId
) {
}
