package com.astra.tracking.session.application.port.out;

import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.CategoryMinutes;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.DailyMinutes;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery.UserMinutes;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LoadSessionStatsPort {

    long sumMinutes(UUID userId);

    long sumMinutesSince(UUID userId, OffsetDateTime start);

    List<DailyMinutes> dailyMinutesSince(UUID userId, OffsetDateTime start);

    List<CategoryMinutes> categoryMinutesSince(UUID userId, OffsetDateTime start);

    List<UserMinutes> minutesByUserSince(OffsetDateTime start);

    List<UserMinutes> minutesByUserSince(OffsetDateTime start, Collection<UUID> userIds);

    Map<UUID, OffsetDateTime> lastStudiedByCourse(UUID userId, Collection<UUID> courseIds);

    boolean existsInCategory(UUID categoryId);
}
