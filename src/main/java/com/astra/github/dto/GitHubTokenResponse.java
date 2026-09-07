package com.astra.github.dto;

public record GitHubTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        Long refreshTokenExpiresInSeconds) {
}
