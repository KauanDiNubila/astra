package com.astra.shared.security;

import com.astra.user.User;
import com.astra.user.UserService;
import com.astra.user.dto.AuthInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                UUID userId = jwtService.extractUserId(header.substring(7));
                AuthInfo authInfo = userService.authInfo(userId);
                if (authInfo != null && !authInfo.banned()) {
                    List<SimpleGrantedAuthority> authorities = authoritiesFor(authInfo.role());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    // OWNER precisa carregar ROLE_ADMIN também (senão o gate hasRole("ADMIN")
    // em /admin/** bloquearia o próprio dono) — expandir aqui em vez de usar
    // um RoleHierarchy do Spring, que exigiria conectar manualmente num
    // WebExpressionAuthorizationManager pra valer dentro de authorizeHttpRequests.
    private static List<SimpleGrantedAuthority> authoritiesFor(String role) {
        List<String> roles = switch (role) {
            case User.ROLE_OWNER -> List.of(User.ROLE_OWNER, User.ROLE_ADMIN, User.ROLE_USER);
            case User.ROLE_ADMIN -> List.of(User.ROLE_ADMIN, User.ROLE_USER);
            default -> List.of(User.ROLE_USER);
        };
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    }
}
