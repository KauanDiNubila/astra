package com.astra.call.adapter.out.user;

import com.astra.call.application.port.out.LoadUserNamePort;
import com.astra.user.application.port.in.LoadUserSummariesQuery;
import com.astra.user.application.port.in.UserSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class UserNameAdapter implements LoadUserNamePort {

    private final LoadUserSummariesQuery loadUserSummariesQuery;

    UserNameAdapter(LoadUserSummariesQuery loadUserSummariesQuery) {
        this.loadUserSummariesQuery = loadUserSummariesQuery;
    }

    @Override
    public String nameOf(UUID userId) {
        UserSummary user = loadUserSummariesQuery.summaries(List.of(userId)).get(userId);
        return user != null ? user.name() : "";
    }
}
