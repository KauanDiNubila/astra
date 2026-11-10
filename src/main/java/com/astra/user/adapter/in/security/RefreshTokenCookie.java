package com.astra.user.adapter.in.security;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
class RefreshTokenCookie {

    static final String NAME = "astra_refresh_token";

    private final Duration maxAge;

    RefreshTokenCookie(@Value("${astra.jwt.refresh-expiration-days}") long refreshExpirationDays) {
        this.maxAge = Duration.ofDays(refreshExpirationDays);
    }

    void attach(HttpServletResponse response, String rawToken) {
        write(response, rawToken, maxAge);
    }

    void clear(HttpServletResponse response) {
        write(response, "", Duration.ZERO);
    }

    private static void write(HttpServletResponse response, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
