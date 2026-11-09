package com.astra.tracking.session.adapter.out.persistence;

import com.astra.tracking.session.domain.Session;
import org.springframework.stereotype.Component;

@Component
class SessionMapper {

    Session toDomain(SessionJpaEntity entity) {
        return Session.withId(entity.getId(), entity.getUserId(), entity.getCategoryId(), entity.getCourseId(),
                entity.getGithubRepositoryId(), entity.getFocusedMinutes(), entity.getStartedAt(),
                entity.getNote(), entity.getCreatedAt());
    }

    SessionJpaEntity toJpaEntity(Session session) {
        return new SessionJpaEntity(session.getId(), session.getUserId(), session.getCategoryId(),
                session.getCourseId(), session.getGithubRepositoryId(), session.getFocusedMinutes(),
                session.getStartedAt(), session.getNote(), session.getCreatedAt());
    }
}
