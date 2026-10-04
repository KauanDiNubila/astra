package com.astra.call.dto;

import java.util.UUID;

public record CallParticipant(UUID userId, String clientId, String name) {
}
