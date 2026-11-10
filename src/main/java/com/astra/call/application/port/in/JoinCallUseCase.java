package com.astra.call.application.port.in;

import java.util.UUID;

public interface JoinCallUseCase {

    void join(UUID userId, String sessionId, UUID callId, String clientId);
}
