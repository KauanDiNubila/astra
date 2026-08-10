package com.astra.tracking.session;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByUserId(UUID userId);

    @Query("select coalesce(sum(s.focusedMinutes), 0) from Session s where s.userId = :userId")
    long sumFocusedMinutes(@Param("userId") UUID userId);

    @Query("select coalesce(sum(s.focusedMinutes), 0) from Session s where s.userId = :userId and s.startedAt >= :start")
    long sumFocusedMinutesSince(@Param("userId") UUID userId, @Param("start") OffsetDateTime start);

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
}
