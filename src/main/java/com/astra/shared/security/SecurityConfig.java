package com.astra.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Slice 1 security: everything is open.
 *
 * The spring-boot-starter-security on the classpath would otherwise lock
 * every endpoint behind generated HTTP Basic credentials. This keeps the
 * tracking endpoints reachable while auth is deferred. Slice 2 replaces
 * this bean with real JWT authentication and per-owner isolation.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
