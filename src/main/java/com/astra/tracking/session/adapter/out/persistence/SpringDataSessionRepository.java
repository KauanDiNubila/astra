package com.astra.tracking.session.adapter.out.persistence;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataSessionRepository extends JpaRepository<SessionJpaEntity, UUID> {

    List<SessionJpaEntity> findByUserId(UUID userId, Pageable pageable);

    boolean existsByCategoryId(UUID categoryId);

    @Query("select coalesce(sum(s.focusedMinutes), 0) from SessionJpaEntity s where s.userId = :userId")
    long sumFocusedMinutes(@Param("userId") UUID userId);

    @Query("""
            select coalesce(sum(s.focusedMinutes), 0) from SessionJpaEntity s
            where s.userId = :userId and s.startedAt >= :start
            """)
    long sumFocusedMinutesSince(@Param("userId") UUID userId, @Param("start") OffsetDateTime start);

    @Query("""
            select s.userId as userId, sum(s.focusedMinutes) as minutes
            from SessionJpaEntity s
            where s.startedAt >= :start
            group by s.userId
            order by sum(s.focusedMinutes) desc
            """)
    List<UserMinutesView> minutesByUserSince(@Param("start") OffsetDateTime start);

    @Query("""
            select s.userId as userId, sum(s.focusedMinutes) as minutes
            from SessionJpaEntity s
            where s.startedAt >= :start and s.userId in :userIds
            group by s.userId
            order by sum(s.focusedMinutes) desc
            """)
    List<UserMinutesView> minutesByUserSince(@Param("start") OffsetDateTime start,
                                             @Param("userIds") Collection<UUID> userIds);

    @Query("""
            select s.courseId as courseId, max(s.startedAt) as lastStudiedAt
            from SessionJpaEntity s
            where s.userId = :userId and s.courseId in :courseIds
            group by s.courseId
            """)
    List<CourseLastStudiedView> lastStudiedByCourse(@Param("userId") UUID userId,
                                                    @Param("courseIds") Collection<UUID> courseIds);

    @Query(value = """
            select (s.started_at at time zone 'America/Sao_Paulo')::date as day,
                   sum(s.focused_minutes) as minutes
            from session s
            where s.user_id = :userId and s.started_at >= :start
            group by (s.started_at at time zone 'America/Sao_Paulo')::date
            order by day
            """, nativeQuery = true)
    List<DailyMinutesView> dailyMinutesSince(@Param("userId") UUID userId, @Param("start") OffsetDateTime start);

    @Query("""
            select s.categoryId as categoryId, sum(s.focusedMinutes) as minutes
            from SessionJpaEntity s
            where s.userId = :userId and s.startedAt >= :start
            group by s.categoryId
            """)
    List<CategoryMinutesView> categoryMinutesSince(@Param("userId") UUID userId,
                                                   @Param("start") OffsetDateTime start);

    interface UserMinutesView {
        UUID getUserId();

        long getMinutes();
    }

    interface CourseLastStudiedView {
        UUID getCourseId();

        OffsetDateTime getLastStudiedAt();
    }

    interface DailyMinutesView {
        LocalDate getDay();

        long getMinutes();
    }

    interface CategoryMinutesView {
        UUID getCategoryId();

        long getMinutes();
    }
}
