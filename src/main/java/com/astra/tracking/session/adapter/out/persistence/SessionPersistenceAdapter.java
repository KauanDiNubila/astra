package com.astra.tracking.session.adapter.out.persistence;

import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.CategoryMinutes;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.DailyMinutes;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery.UserMinutes;
import com.astra.tracking.session.application.port.out.CreateSessionPort;
import com.astra.tracking.session.application.port.out.DeleteSessionPort;
import com.astra.tracking.session.application.port.out.ListSessionsPort;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.application.port.out.LoadSessionStatsPort;
import com.astra.tracking.session.application.port.out.UpdateSessionRepositoryPort;
import com.astra.tracking.session.domain.Session;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
class SessionPersistenceAdapter implements CreateSessionPort, LoadSessionPort, ListSessionsPort,
        UpdateSessionRepositoryPort, DeleteSessionPort, LoadSessionStatsPort {

    private final SpringDataSessionRepository repository;
    private final SessionMapper mapper;

    SessionPersistenceAdapter(SpringDataSessionRepository repository, SessionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Session create(Session session) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toJpaEntity(session)));
    }

    @Override
    public Optional<Session> load(UUID sessionId) {
        return repository.findById(sessionId).map(mapper::toDomain);
    }

    @Override
    public List<Session> listNewestFirst(UUID userId, int page, int size) {
        return repository.findByUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt")))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Session> listAllNewestFirst(UUID userId) {
        return repository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void updateRepository(Session session) {
        repository.findById(session.getId())
                .orElseThrow()
                .setGithubRepositoryId(session.getGithubRepositoryId());
    }

    @Override
    public void delete(UUID sessionId) {
        repository.deleteById(sessionId);
    }

    @Override
    public long sumMinutes(UUID userId) {
        return repository.sumFocusedMinutes(userId);
    }

    @Override
    public long sumMinutesSince(UUID userId, OffsetDateTime start) {
        return repository.sumFocusedMinutesSince(userId, start);
    }

    @Override
    public List<DailyMinutes> dailyMinutesSince(UUID userId, OffsetDateTime start) {
        return repository.dailyMinutesSince(userId, start).stream()
                .map(v -> new DailyMinutes(v.getDay(), v.getMinutes()))
                .toList();
    }

    @Override
    public List<CategoryMinutes> categoryMinutesSince(UUID userId, OffsetDateTime start) {
        return repository.categoryMinutesSince(userId, start).stream()
                .map(v -> new CategoryMinutes(v.getCategoryId(), v.getMinutes()))
                .toList();
    }

    @Override
    public List<UserMinutes> minutesByUserSince(OffsetDateTime start) {
        return toUserMinutes(repository.minutesByUserSince(start));
    }

    @Override
    public List<UserMinutes> minutesByUserSince(OffsetDateTime start, Collection<UUID> userIds) {
        return toUserMinutes(repository.minutesByUserSince(start, userIds));
    }

    @Override
    public Map<UUID, OffsetDateTime> lastStudiedByCourse(UUID userId, Collection<UUID> courseIds) {
        return repository.lastStudiedByCourse(userId, courseIds).stream()
                .collect(Collectors.toMap(
                        SpringDataSessionRepository.CourseLastStudiedView::getCourseId,
                        SpringDataSessionRepository.CourseLastStudiedView::getLastStudiedAt));
    }

    @Override
    public boolean existsInCategory(UUID categoryId) {
        return repository.existsByCategoryId(categoryId);
    }

    private List<UserMinutes> toUserMinutes(List<SpringDataSessionRepository.UserMinutesView> views) {
        return views.stream().map(v -> new UserMinutes(v.getUserId(), v.getMinutes())).toList();
    }
}
