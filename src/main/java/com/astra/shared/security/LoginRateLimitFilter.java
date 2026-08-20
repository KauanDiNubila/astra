package com.astra.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, Window> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(request.getRequestURI())) {
            Window window = attemptsByIp.computeIfAbsent(request.getRemoteAddr(), key -> new Window());
            if (window.tooManyAttempts()) {
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
        private volatile Instant windowStart = Instant.now();

        synchronized boolean tooManyAttempts() {
            if (Instant.now().isAfter(windowStart.plusSeconds(WINDOW_SECONDS))) {
                windowStart = Instant.now();
                count.set(0);
            }
            return count.incrementAndGet() > MAX_ATTEMPTS;
        }
    }
}
