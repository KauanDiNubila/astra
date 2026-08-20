package com.astra.tracking.session;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionStatsService {

    private final SessionRepository sessionRepository;

    public SessionStatsService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public long totalFocusedMinutes(UUID userId) {
        return sessionRepository.sumFocusedMinutes(userId);
    }

    @Transactional(readOnly = true)
    public long focusedMinutesSince(UUID userId, OffsetDateTime start) {
        return sessionRepository.sumFocusedMinutesSince(userId, start);
    }

    @Transactional(readOnly = true)
    public List<DailyMinutes> dailyMinutesSince(UUID userId, OffsetDateTime start) {
        return sessionRepository.dailyMinutesSince(userId, start).stream()
                .map(v -> new DailyMinutes(v.getDay(), v.getMinutes()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserMinutes> rankingSince(OffsetDateTime start) {
        return sessionRepository.rankingSince(start);
    }

    @Transactional(readOnly = true)
    public List<UserMinutes> rankingSinceForUsers(OffsetDateTime start, Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return sessionRepository.rankingSinceForUsers(start, userIds);
    }
}
