package com.astra.tracking.session.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface GetSessionWindowQuery {

    SessionWindow get(UUID userId, UUID sessionId);

    record SessionWindow(OffsetDateTime startedAt, OffsetDateTime endedAt, UUID githubRepositoryId) {
    }
}
