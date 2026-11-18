package com.astra.user.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;
    private static final int CLEANUP_THRESHOLD = 10_000;
    private static final Map<String, RequestMatcher> LIMITED_ENDPOINTS = Stream.of("/auth/login",
                    "/auth/forgot-password", "/auth/reset-password")
            .collect(Collectors.toMap(path -> path,
                    path -> PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, path)));

    private final ConcurrentHashMap<String, Window> attemptsByIp = new ConcurrentHashMap<>();
    private final Clock clock;

    AuthRateLimitFilter() {
        this(Clock.systemUTC());
    }

    AuthRateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    int trackedAddresses() {
        return attemptsByIp.size();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> limitedPath = limitedPath(request);
        if (limitedPath.isPresent()) {
            if (attemptsByIp.size() > CLEANUP_THRESHOLD) {
                Instant now = clock.instant();
                attemptsByIp.values().removeIf(w -> w.isExpired(now));
            }
            Window window = attemptsByIp.computeIfAbsent(limitedPath.get() + "|" + request.getRemoteAddr(),
                    key -> new Window(clock.instant()));
            if (window.tooManyAttempts(clock.instant())) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Muitas tentativas. Tente novamente em instantes.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static Optional<String> limitedPath(HttpServletRequest request) {
        return LIMITED_ENDPOINTS.entrySet().stream()
                .filter(endpoint -> endpoint.getValue().matches(request))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private static final class Window {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant windowStart;

        Window(Instant now) {
            this.windowStart = now;
        }

        boolean isExpired(Instant now) {
            return now.isAfter(windowStart.plusSeconds(WINDOW_SECONDS));
        }

        synchronized boolean tooManyAttempts(Instant now) {
            if (isExpired(now)) {
                windowStart = now;
                count.set(0);
            }
            return count.incrementAndGet() > MAX_ATTEMPTS;
        }
    }
}
