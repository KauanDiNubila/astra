package com.astra.call.domain;

import java.util.UUID;

public record StaleMember(UUID callId, UUID userId, String clientId, String sessionId) {
}
