package com.astra.user.adapter.in.web;

import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.shared.security.JwtService;
import com.astra.user.application.port.in.GetAuthInfoQuery;
import com.astra.user.application.port.in.GetUserQuery;
import com.astra.user.application.port.in.LogInUseCase;
import com.astra.user.application.port.in.RegisterUserUseCase;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.service.DesktopLoginService;
import com.astra.user.service.RefreshTokenService;
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

@RestController
@RequestMapping("/auth")
class AuthController {

    private static final String REFRESH_COOKIE = RefreshTokenService.REFRESH_COOKIE;
    private static final Set<String> DESKTOP_PROVIDERS = Set.of("google", "github");

    private final RegisterUserUseCase registerUserUseCase;
    private final LogInUseCase logInUseCase;
    private final GetUserQuery getUserQuery;
    private final GetAuthInfoQuery getAuthInfoQuery;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final DesktopLoginService desktopLoginService;

    AuthController(RegisterUserUseCase registerUserUseCase, LogInUseCase logInUseCase, GetUserQuery getUserQuery,
                   GetAuthInfoQuery getAuthInfoQuery, JwtService jwtService, RefreshTokenService refreshTokenService,
                   DesktopLoginService desktopLoginService) {
        this.registerUserUseCase = registerUserUseCase;
        this.logInUseCase = logInUseCase;
        this.getUserQuery = getUserQuery;
        this.getAuthInfoQuery = getAuthInfoQuery;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.desktopLoginService = desktopLoginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return registerUserUseCase.register(request.toCommand());
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        UserResponse user = logInUseCase.logIn(request.toCommand());
        return issueTokens(user.id(), user, response);
    }

    @PostMapping("/refresh")
    AuthResponse refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                         HttpServletResponse response) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token ausente");
        }
        UUID userId = refreshTokenService.rotate(refreshToken);
        return issueTokens(userId, getUserQuery.get(userId), response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
                HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
        refreshTokenService.clearCookie(response);
    }

    @GetMapping("/desktop/login")
    void desktopLogin(@RequestParam String provider, @RequestParam String challenge, HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        if (!DESKTOP_PROVIDERS.contains(provider) || !DesktopLoginService.isValidChallenge(challenge)) {
            throw new ConflictException("Pedido de login do app inválido");
        }
        request.getSession(true).setAttribute(DesktopLoginService.SESSION_ATTRIBUTE, challenge);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @PostMapping("/desktop/exchange")
    AuthResponse desktopExchange(@Valid @RequestBody DesktopExchangeRequest request, HttpServletResponse response) {
        UUID userId = desktopLoginService.redeem(request.code(), request.verifier());
        return issueTokens(userId, getUserQuery.get(userId), response);
    }

    private AuthResponse issueTokens(UUID userId, UserResponse user, HttpServletResponse response) {
        if (!getAuthInfoQuery.canSignIn(userId)) {
            throw new UnauthorizedException("Conta banida");
        }
        String accessToken = jwtService.generateToken(userId);
        refreshTokenService.issueAndAttachCookie(userId, response);
        return new AuthResponse(accessToken, user);
    }
}
