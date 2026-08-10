package com.astra.tracking.session;

import java.time.OffsetDateTime;
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
}
