package com.astra.shared.security;

import com.astra.user.application.port.in.GetAuthInfoQuery;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final GetAuthInfoQuery getAuthInfoQuery;

    public JwtAuthenticationFilter(JwtService jwtService, GetAuthInfoQuery getAuthInfoQuery) {
        this.jwtService = jwtService;
        this.getAuthInfoQuery = getAuthInfoQuery;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                UUID userId = jwtService.extractUserId(header.substring(7));
                getAuthInfoQuery.authInfo(userId)
                        .filter(info -> !info.banned())
                        .ifPresentOrElse(info -> SecurityContextHolder.getContext().setAuthentication(
                                        new UsernamePasswordAuthenticationToken(userId, null, info.roles().stream()
                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                .toList())),
                                SecurityContextHolder::clearContext);
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
