package com.astra.stats.application.port.out;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LoadMinutesByUserPort {

    List<UserMinutes> since(OffsetDateTime start);

    List<UserMinutes> sinceForUsers(OffsetDateTime start, Collection<UUID> userIds);

    record UserMinutes(UUID userId, long minutes) {
    }
}
