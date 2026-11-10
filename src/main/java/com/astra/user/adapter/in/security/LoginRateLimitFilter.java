package com.astra.user.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final ConcurrentHashMap<String, Window> attemptsByIp = new ConcurrentHashMap<>();
    private final Clock clock;

    LoginRateLimitFilter() {
        this(Clock.systemUTC());
    }

    LoginRateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    int trackedAddresses() {
        return attemptsByIp.size();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(request.getRequestURI())) {
            if (attemptsByIp.size() > CLEANUP_THRESHOLD) {
                Instant now = clock.instant();
                attemptsByIp.values().removeIf(w -> w.isExpired(now));
            }
            Window window = attemptsByIp.computeIfAbsent(request.getRemoteAddr(), key -> new Window(clock.instant()));
            if (window.tooManyAttempts(clock.instant())) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Muitas tentativas de login. Tente novamente em instantes.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
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
