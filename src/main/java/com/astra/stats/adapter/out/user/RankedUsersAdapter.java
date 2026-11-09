package com.astra.stats.adapter.out.user;

import com.astra.stats.application.port.out.LoadRankedUsersPort;
import com.astra.user.application.port.in.LoadUserSummariesQuery;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class RankedUsersAdapter implements LoadRankedUsersPort {

    private final LoadUserSummariesQuery loadUserSummariesQuery;

    RankedUsersAdapter(LoadUserSummariesQuery loadUserSummariesQuery) {
        this.loadUserSummariesQuery = loadUserSummariesQuery;
    }

    @Override
    public Map<UUID, RankedUser> loadByIds(Collection<UUID> userIds) {
        return loadUserSummariesQuery.summaries(userIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new RankedUser(e.getValue().name(), e.getValue().staff())));
    }
}
