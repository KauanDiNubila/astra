package com.astra.user;

import com.astra.shared.exception.UnauthorizedException;
import com.astra.shared.security.JwtService;
import com.astra.user.dto.AuthResponse;
import com.astra.user.dto.LoginRequest;
import com.astra.user.dto.RegisterRequest;
import com.astra.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "astra_refresh_token";

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final long refreshExpirationDays;

    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService,
            @Value("${astra.jwt.refresh-expiration-days}") long refreshExpirationDays) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        UUID userId = userService.login(request);
        return issueTokens(userId, response);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token ausente");
        }
        UUID userId = refreshTokenService.rotate(refreshToken);
        return issueTokens(userId, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
        clearRefreshCookie(response);
    }

    private AuthResponse issueTokens(UUID userId, HttpServletResponse response) {
        String accessToken = jwtService.generateToken(userId);
        String refreshToken = refreshTokenService.issue(userId);
        setRefreshCookie(response, refreshToken);
        return new AuthResponse(accessToken);
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(Duration.ofDays(refreshExpirationDays))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
