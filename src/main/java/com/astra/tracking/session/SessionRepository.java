package com.astra.tracking.session;

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
}
