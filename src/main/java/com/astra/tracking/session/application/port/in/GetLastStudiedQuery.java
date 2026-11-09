package com.astra.tracking.session.application.port.in;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface GetLastStudiedQuery {

    Map<UUID, OffsetDateTime> byCourse(UUID userId, Collection<UUID> courseIds);
}
