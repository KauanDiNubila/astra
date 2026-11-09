package com.astra.learning.adapter.out.session;

import com.astra.learning.application.port.out.LoadLastStudiedPort;
import com.astra.tracking.session.application.port.in.GetLastStudiedQuery;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class LastStudiedAdapter implements LoadLastStudiedPort {

    private final GetLastStudiedQuery getLastStudiedQuery;

    LastStudiedAdapter(GetLastStudiedQuery getLastStudiedQuery) {
        this.getLastStudiedQuery = getLastStudiedQuery;
    }

    @Override
    public Map<UUID, OffsetDateTime> byCourse(UUID userId, Collection<UUID> courseIds) {
        return getLastStudiedQuery.byCourse(userId, courseIds);
    }
}
