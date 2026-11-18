package com.astra.user.domain;

import com.astra.shared.exception.ConflictException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PasswordResetToken {

    public static final Duration LIFETIME = Duration.ofMinutes(30);
    public static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    public static final String INVALID_LINK = "Link de redefinição inválido ou expirado";

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime expiresAt;
    private final OffsetDateTime usedAt;

    private PasswordResetToken(UUID id, UUID userId, String tokenHash, OffsetDateTime createdAt,
                               OffsetDateTime expiresAt, OffsetDateTime usedAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.usedAt = usedAt;
    }

    public static PasswordResetToken issue(UUID userId, String rawToken, OffsetDateTime now) {
        return new PasswordResetToken(null, userId, RefreshToken.hash(rawToken), now, now.plus(LIFETIME), null);
    }

    public static PasswordResetToken withId(UUID id, UUID userId, String tokenHash, OffsetDateTime createdAt,
                                            OffsetDateTime expiresAt, OffsetDateTime usedAt) {
        return new PasswordResetToken(Objects.requireNonNull(id), userId, tokenHash, createdAt, expiresAt, usedAt);
    }

    public boolean isWithinResendCooldown(OffsetDateTime now) {
        return createdAt.isAfter(now.minus(RESEND_COOLDOWN));
    }

    public void requireUsable(OffsetDateTime now) {
        if (usedAt != null || !expiresAt.isAfter(now)) {
            throw new ConflictException(INVALID_LINK);
        }
    }
}
