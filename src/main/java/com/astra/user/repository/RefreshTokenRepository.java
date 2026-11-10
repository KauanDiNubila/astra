package com.astra.user.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.astra.user.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken r set r.revokedAt = coalesce(r.revokedAt, :now), r.rotated = false
            where r.userId = :userId
            """)
    void revokeAllForUser(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
