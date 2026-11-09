package com.astra.user.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record OAuthConnection(UUID userId, String provider, String providerUserId, OffsetDateTime createdAt) {

    public OAuthConnection {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(providerUserId);
    }

    public static OAuthConnection link(UUID userId, String provider, String providerUserId) {
        return new OAuthConnection(userId, provider, providerUserId, null);
    }
}
