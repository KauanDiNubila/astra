package com.astra.github.domain;

import java.util.Objects;

public record TokenGrant(String accessToken, String refreshToken, long expiresInSeconds,
                         Long refreshExpiresInSeconds) {

    public TokenGrant {
        Objects.requireNonNull(accessToken);
    }
}
