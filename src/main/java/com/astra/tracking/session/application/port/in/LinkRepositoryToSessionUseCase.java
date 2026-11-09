package com.astra.tracking.session.application.port.in;

import java.util.UUID;

public interface LinkRepositoryToSessionUseCase {

    SessionResponse link(UUID userId, UUID sessionId, UUID repositoryId);
}
