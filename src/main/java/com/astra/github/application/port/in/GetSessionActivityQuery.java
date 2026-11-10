package com.astra.github.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface GetSessionActivityQuery {

    SessionActivityResponse activityFor(UUID userId, OffsetDateTime from, OffsetDateTime to, UUID preferredRepositoryId);
}
