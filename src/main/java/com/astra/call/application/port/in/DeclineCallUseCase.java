package com.astra.call.application.port.in;

import java.util.UUID;

public interface DeclineCallUseCase {

    void decline(UUID userId, UUID callId);
}
