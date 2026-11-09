package com.astra.tracking.session.application.service;

import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery;
import com.astra.tracking.session.application.port.in.GetLastStudiedQuery;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery;
import com.astra.tracking.session.application.port.in.GetSessionWindowQuery;
import com.astra.tracking.session.application.port.in.HasSessionsInCategoryQuery;
import com.astra.tracking.session.application.port.in.ListSessionsQuery;
import com.astra.tracking.session.application.port.in.SessionResponse;
import com.astra.tracking.session.application.port.out.ListSessionsPort;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.application.port.out.LoadSessionStatsPort;
import com.astra.tracking.session.domain.Session;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class SessionQueryService implements ListSessionsQuery, GetSessionWindowQuery, GetFocusedMinutesQuery,
        GetMinutesByUserQuery, GetLastStudiedQuery, HasSessionsInCategoryQuery {

    static final int MAX_PAGE_SIZE = 100;

    private final ListSessionsPort listSessionsPort;
    private final LoadSessionPort loadSessionPort;
    private final LoadSessionStatsPort loadSessionStatsPort;

    SessionQueryService(ListSessionsPort listSessionsPort, LoadSessionPort loadSessionPort,
                        LoadSessionStatsPort loadSessionStatsPort) {
        this.listSessionsPort = listSessionsPort;
        this.loadSessionPort = loadSessionPort;
        this.loadSessionStatsPort = loadSessionStatsPort;
    }

    @Override
    public List<SessionResponse> list(UUID userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return listSessionsPort.listNewestFirst(userId, safePage, safeSize).stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Override
    public List<SessionResponse> listAll(UUID userId) {
        return listSessionsPort.listAllNewestFirst(userId).stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Override
    public SessionWindow get(UUID userId, UUID sessionId) {
        Session session = OwnedSession.load(loadSessionPort, userId, sessionId);
        return new SessionWindow(session.getStartedAt(), session.endedAt(), session.getGithubRepositoryId());
    }

    @Override
    public long total(UUID userId) {
        return loadSessionStatsPort.sumMinutes(userId);
    }

    @Override
    public long since(UUID userId, OffsetDateTime start) {
        return loadSessionStatsPort.sumMinutesSince(userId, start);
    }

    @Override
    public List<DailyMinutes> dailySince(UUID userId, OffsetDateTime start) {
        return loadSessionStatsPort.dailyMinutesSince(userId, start);
    }

    @Override
    public List<CategoryMinutes> byCategorySince(UUID userId, OffsetDateTime start) {
        return loadSessionStatsPort.categoryMinutesSince(userId, start);
    }

    @Override
    public List<UserMinutes> since(OffsetDateTime start) {
        return loadSessionStatsPort.minutesByUserSince(start);
    }

    @Override
    public List<UserMinutes> sinceForUsers(OffsetDateTime start, Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return loadSessionStatsPort.minutesByUserSince(start, userIds);
    }

    @Override
    public Map<UUID, OffsetDateTime> byCourse(UUID userId, Collection<UUID> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return loadSessionStatsPort.lastStudiedByCourse(userId, courseIds);
    }

    @Override
    public boolean hasSessionsInCategory(UUID categoryId) {
        return loadSessionStatsPort.existsInCategory(categoryId);
    }
}
