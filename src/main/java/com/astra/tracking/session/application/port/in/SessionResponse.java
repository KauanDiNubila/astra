package com.astra.tracking.session.application.port.in;

import com.astra.tracking.session.domain.Session;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID categoryId,
        UUID courseId,
        UUID githubRepositoryId,
        int focusedMinutes,
        OffsetDateTime startedAt,
        String note,
        OffsetDateTime createdAt
) {

    public static SessionResponse from(Session session) {
        return new SessionResponse(session.getId(), session.getCategoryId(), session.getCourseId(),
                session.getGithubRepositoryId(), session.getFocusedMinutes(), session.getStartedAt(),
                session.getNote(), session.getCreatedAt());
    }
}
