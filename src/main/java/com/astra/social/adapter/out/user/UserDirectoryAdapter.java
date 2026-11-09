package com.astra.social.adapter.out.user;

import com.astra.social.application.port.out.FindUserByHandlePort;
import com.astra.social.application.port.out.LoadUserSummariesPort;
import com.astra.user.application.port.in.FindUserByHandleQuery;
import com.astra.user.application.port.in.LoadUserSummariesQuery;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class UserDirectoryAdapter implements FindUserByHandlePort, LoadUserSummariesPort {

    private final FindUserByHandleQuery findUserByHandleQuery;
    private final LoadUserSummariesQuery loadUserSummariesQuery;

    UserDirectoryAdapter(FindUserByHandleQuery findUserByHandleQuery, LoadUserSummariesQuery loadUserSummariesQuery) {
        this.findUserByHandleQuery = findUserByHandleQuery;
        this.loadUserSummariesQuery = loadUserSummariesQuery;
    }

    @Override
    public Optional<UUID> findIdByHandle(String name, String tag) {
        return findUserByHandleQuery.findIdByHandle(name, tag);
    }

    @Override
    public Map<UUID, UserSummary> loadByIds(Collection<UUID> userIds) {
        return loadUserSummariesQuery.summaries(userIds).values().stream()
                .collect(Collectors.toMap(com.astra.user.application.port.in.UserSummary::id,
                        v -> new UserSummary(v.id(), v.name(), v.tag(), v.bio(), v.staff())));
    }
}
