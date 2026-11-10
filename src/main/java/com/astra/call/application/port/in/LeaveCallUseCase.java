package com.astra.call.application.port.in;

import java.util.UUID;

public interface LeaveCallUseCase {

    void leave(UUID userId, UUID callId, String clientId);
}
