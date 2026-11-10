package com.astra.call.domain;

import java.util.UUID;

public record CallParticipant(UUID userId, String clientId, String name) {
}
