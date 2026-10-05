package com.astra.user.controller;

import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.shared.security.JwtService;
import com.astra.user.dto.AuthResponse;
import com.astra.user.dto.DesktopExchangeRequest;
import com.astra.user.dto.LoginRequest;
import com.astra.user.dto.RegisterRequest;
import com.astra.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.astra.user.service.DesktopLoginService;
import com.astra.user.service.RefreshTokenService;
import com.astra.user.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = RefreshTokenService.REFRESH_COOKIE;
    private static final Set<String> DESKTOP_PROVIDERS = Set.of("google", "github");

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final DesktopLoginService desktopLoginService;

    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService,
            DesktopLoginService desktopLoginService) {
        this.desktopLoginService = desktopLoginService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        UserResponse user = userService.login(request);
        return issueTokens(user.id(), user, response);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token ausente");
        }
        UUID userId = refreshTokenService.rotate(refreshToken);
        // Devolve o usuário junto do token novo — evita o front precisar de
        // uma chamada extra a /me toda vez que a aba recarrega.
        UserResponse user = userService.get(userId);
        return issueTokens(userId, user, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
        refreshTokenService.clearCookie(response);
    }

    @GetMapping("/desktop/login")
    public void desktopLogin(@RequestParam String provider, @RequestParam String challenge,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!DESKTOP_PROVIDERS.contains(provider) || !DesktopLoginService.isValidChallenge(challenge)) {
            throw new ConflictException("Pedido de login do app inválido");
        }
        request.getSession(true).setAttribute(DesktopLoginService.SESSION_ATTRIBUTE, challenge);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @PostMapping("/desktop/exchange")
    public AuthResponse desktopExchange(@Valid @RequestBody DesktopExchangeRequest request,
            HttpServletResponse response) {
        UUID userId = desktopLoginService.redeem(request.code(), request.verifier());
        return issueTokens(userId, userService.get(userId), response);
    }

    private AuthResponse issueTokens(UUID userId, UserResponse user, HttpServletResponse response) {
        String accessToken = jwtService.generateToken(userId);
        refreshTokenService.issueAndAttachCookie(userId, response);
        return new AuthResponse(accessToken, user);
    }
}
