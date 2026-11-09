package com.astra.stats.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadRankedUsersPort {

    Map<UUID, RankedUser> loadByIds(Collection<UUID> userIds);

    record RankedUser(String name, boolean admin) {
    }
}
