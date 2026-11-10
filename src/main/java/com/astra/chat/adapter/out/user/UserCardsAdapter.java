package com.astra.chat.adapter.out.user;

import com.astra.chat.application.port.out.LoadUserCardsPort;
import com.astra.user.application.port.in.LoadUserSummariesQuery;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class UserCardsAdapter implements LoadUserCardsPort {

    private final LoadUserSummariesQuery loadUserSummariesQuery;

    UserCardsAdapter(LoadUserSummariesQuery loadUserSummariesQuery) {
        this.loadUserSummariesQuery = loadUserSummariesQuery;
    }

    @Override
    public Map<UUID, UserCard> cards(Collection<UUID> userIds) {
        return loadUserSummariesQuery.summaries(userIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new UserCard(e.getValue().name(),
                        e.getValue().tag(), e.getValue().bio(), e.getValue().staff())));
    }
}
