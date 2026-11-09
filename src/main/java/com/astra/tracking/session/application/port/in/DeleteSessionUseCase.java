package com.astra.tracking.session.application.port.in;

import java.util.UUID;

public interface DeleteSessionUseCase {

    void delete(UUID userId, UUID sessionId);
}
