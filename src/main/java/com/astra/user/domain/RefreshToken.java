package com.astra.user.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RefreshToken {

    public static final Duration ROTATION_LEEWAY = Duration.ofSeconds(10);

    public enum Rotation {
        ROTATED,
        CONCURRENT,
        REUSED,
        EXPIRED
    }

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;
    private boolean rotated;

    private RefreshToken(UUID id, UUID userId, String tokenHash, OffsetDateTime expiresAt, OffsetDateTime revokedAt,
                         boolean rotated) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revokedAt = revokedAt;
        this.rotated = rotated;
    }

    public static RefreshToken issue(UUID userId, String rawToken, OffsetDateTime now, Duration lifetime) {
        return new RefreshToken(null, userId, hash(rawToken), now.plus(lifetime), null, false);
    }

    public static RefreshToken withId(UUID id, UUID userId, String tokenHash, OffsetDateTime expiresAt,
                                      OffsetDateTime revokedAt, boolean rotated) {
        return new RefreshToken(Objects.requireNonNull(id), userId, tokenHash, expiresAt, revokedAt, rotated);
    }

    public static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }

    public Rotation rotate(OffsetDateTime now) {
        if (revokedAt != null) {
            return rotated && revokedAt.isAfter(now.minus(ROTATION_LEEWAY)) ? Rotation.CONCURRENT : Rotation.REUSED;
        }
        if (!expiresAt.isAfter(now)) {
            return Rotation.EXPIRED;
        }
        revokedAt = now;
        rotated = true;
        return Rotation.ROTATED;
    }

    public void revoke(OffsetDateTime now) {
        revokedAt = now;
        rotated = false;
    }
}
