package com.astra.tracking.session.application.port.in;

import java.util.UUID;

public interface UnlinkRepositoryFromSessionUseCase {

    SessionResponse unlink(UUID userId, UUID sessionId);
}
