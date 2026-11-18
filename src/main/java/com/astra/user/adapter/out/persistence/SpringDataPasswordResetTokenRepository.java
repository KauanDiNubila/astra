package com.astra.user.adapter.out.persistence;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {

    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    Optional<PasswordResetTokenJpaEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("""
            update PasswordResetTokenJpaEntity t set t.usedAt = :now
            where t.userId = :userId and t.usedAt is null
            """)
    int invalidateAllForUser(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
