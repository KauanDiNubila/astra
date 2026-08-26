package com.astra.tracking.session;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByUserId(UUID userId);

    boolean existsByCategoryId(UUID categoryId);

    @Query("select coalesce(sum(s.focusedMinutes), 0) from Session s where s.userId = :userId")
    long sumFocusedMinutes(@Param("userId") UUID userId);

    @Query("select coalesce(sum(s.focusedMinutes), 0) from Session s where s.userId = :userId and s.startedAt >= :start")
    long sumFocusedMinutesSince(@Param("userId") UUID userId, @Param("start") OffsetDateTime start);

    @Query("""
            select new com.astra.tracking.session.UserMinutes(s.userId, sum(s.focusedMinutes))
            from Session s
            where s.startedAt >= :start
            group by s.userId
            order by sum(s.focusedMinutes) desc
            """)
    List<UserMinutes> rankingSince(@Param("start") OffsetDateTime start);

    @Query("""
            select new com.astra.tracking.session.UserMinutes(s.userId, sum(s.focusedMinutes))
            from Session s
            where s.startedAt >= :start and s.userId in :userIds
            group by s.userId
            order by sum(s.focusedMinutes) desc
            """)
    List<UserMinutes> rankingSinceForUsers(@Param("start") OffsetDateTime start, @Param("userIds") Collection<UUID> userIds);

    @Query(value = """
            select (s.started_at at time zone 'America/Sao_Paulo')::date as day,
                   sum(s.focused_minutes) as minutes
            from session s
            where s.user_id = :userId and s.started_at >= :start
            group by (s.started_at at time zone 'America/Sao_Paulo')::date
            order by day
            """, nativeQuery = true)
    List<DailyMinutesView> dailyMinutesSince(@Param("userId") UUID userId, @Param("start") OffsetDateTime start);

    interface DailyMinutesView {
        LocalDate getDay();

        long getMinutes();
    }

    @Query("""
            select s.category.id as categoryId, sum(s.focusedMinutes) as minutes
            from Session s
            where s.userId = :userId and s.startedAt >= :start
            group by s.category.id
            """)
    List<CategoryMinutesView> categoryMinutesSince(@Param("userId") UUID userId, @Param("start") OffsetDateTime start);

    interface CategoryMinutesView {
        UUID getCategoryId();

        long getMinutes();
    }
}
