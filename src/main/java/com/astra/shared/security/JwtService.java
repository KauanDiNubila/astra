package com.astra.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String PURPOSE_CLAIM = "purpose";

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${astra.jwt.secret}") String secret,
                      @Value("${astra.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    // Um token de sessao normal nunca deve carregar um claim de "purpose" -
    // se carregar, e um token de proposito unico (ex: state do fluxo de
    // conexao com GitHub) que vazou/foi reaproveitado indevidamente, e nao
    // pode autenticar uma requisicao normal.
    public UUID extractUserId(String token) {
        Claims claims = parse(token);
        if (claims.get(PURPOSE_CLAIM) != null) {
            throw new JwtException("Token de proposito nao pode ser usado como sessao");
        }
        return UUID.fromString(claims.getSubject());
    }

    // Token de curtissima duracao pra carregar o userId atraves de um
    // redirect cru de navegador (sem cookie/Bearer disponivel), ex: o
    // `state` do fluxo OAuth de "conectar GitHub". Nunca aceito por
    // extractUserId - so por extractPurposeUserId com o mesmo purpose.
    public String generatePurposeToken(UUID userId, String purpose, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(PURPOSE_CLAIM, purpose)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public UUID extractPurposeUserId(String token, String expectedPurpose) {
        Claims claims = parse(token);
        if (!expectedPurpose.equals(claims.get(PURPOSE_CLAIM))) {
            throw new JwtException("Token nao tem o proposito esperado");
        }
        return UUID.fromString(claims.getSubject());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
