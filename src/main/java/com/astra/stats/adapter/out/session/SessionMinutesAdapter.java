package com.astra.stats.adapter.out.session;

import com.astra.stats.application.port.out.LoadFocusedMinutesPort;
import com.astra.stats.application.port.out.LoadMinutesByUserPort;
import com.astra.stats.domain.CategoryMinutes;
import com.astra.stats.domain.DayMinutes;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SessionMinutesAdapter implements LoadFocusedMinutesPort, LoadMinutesByUserPort {

    private final GetFocusedMinutesQuery getFocusedMinutesQuery;
    private final GetMinutesByUserQuery getMinutesByUserQuery;

    SessionMinutesAdapter(GetFocusedMinutesQuery getFocusedMinutesQuery, GetMinutesByUserQuery getMinutesByUserQuery) {
        this.getFocusedMinutesQuery = getFocusedMinutesQuery;
        this.getMinutesByUserQuery = getMinutesByUserQuery;
    }

    @Override
    public long totalMinutes(UUID userId) {
        return getFocusedMinutesQuery.total(userId);
    }

    @Override
    public List<DayMinutes> dailySince(UUID userId, OffsetDateTime start) {
        return getFocusedMinutesQuery.dailySince(userId, start).stream()
                .map(d -> new DayMinutes(d.day(), d.minutes()))
                .toList();
    }

    @Override
    public List<CategoryMinutes> byCategorySince(UUID userId, OffsetDateTime start) {
        return getFocusedMinutesQuery.byCategorySince(userId, start).stream()
                .map(c -> new CategoryMinutes(c.categoryId(), c.minutes()))
                .toList();
    }

    @Override
    public List<UserMinutes> since(OffsetDateTime start) {
        return toUserMinutes(getMinutesByUserQuery.since(start));
    }

    @Override
    public List<UserMinutes> sinceForUsers(OffsetDateTime start, Collection<UUID> userIds) {
        return toUserMinutes(getMinutesByUserQuery.sinceForUsers(start, userIds));
    }

    private List<UserMinutes> toUserMinutes(List<GetMinutesByUserQuery.UserMinutes> rows) {
        return rows.stream().map(r -> new UserMinutes(r.userId(), r.minutes())).toList();
    }
}
