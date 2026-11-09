package com.astra.learning.application.port.out;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadLastStudiedPort {

    Map<UUID, OffsetDateTime> byCourse(UUID userId, Collection<UUID> courseIds);
}
