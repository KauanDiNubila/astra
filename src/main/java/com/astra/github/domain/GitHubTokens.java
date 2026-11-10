package com.astra.github.domain;

import java.time.OffsetDateTime;

public record GitHubTokens(String accessToken, String refreshToken, OffsetDateTime accessExpiresAt,
                           OffsetDateTime refreshExpiresAt, boolean readable) {

    public static GitHubTokens granted(TokenGrant grant, OffsetDateTime now) {
        return new GitHubTokens(grant.accessToken(), grant.refreshToken(), now.plusSeconds(grant.expiresInSeconds()),
                grant.refreshExpiresInSeconds() == null ? null : now.plusSeconds(grant.refreshExpiresInSeconds()),
                true);
    }

    public static GitHubTokens unreadable(OffsetDateTime accessExpiresAt, OffsetDateTime refreshExpiresAt) {
        return new GitHubTokens(null, null, accessExpiresAt, refreshExpiresAt, false);
    }
}
