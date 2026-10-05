package com.astra.user.service;

import com.astra.shared.exception.UnauthorizedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DesktopLoginService {

    public static final String SESSION_ATTRIBUTE = "astraDesktopChallenge";

    private static final Pattern CHALLENGE = Pattern.compile("^[A-Za-z0-9_-]{43}$");
    private static final Pattern VERIFIER = Pattern.compile("^[A-Za-z0-9_-]{43,128}$");
    private static final int CLEANUP_THRESHOLD = 500;

    private final Duration codeTtl;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingCode> codes = new ConcurrentHashMap<>();

    public DesktopLoginService(@Value("${astra.desktop-login.code-ttl-seconds:120}") long codeTtlSeconds) {
        this.codeTtl = Duration.ofSeconds(codeTtlSeconds);
    }

    public static boolean isValidChallenge(String challenge) {
        return challenge != null && CHALLENGE.matcher(challenge).matches();
    }

    public String createCode(UUID userId, String challenge) {
        if (codes.size() > CLEANUP_THRESHOLD) {
            Instant now = Instant.now();
            codes.values().removeIf(pending -> pending.expiresAt().isBefore(now));
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        codes.put(code, new PendingCode(userId, challenge, Instant.now().plus(codeTtl)));
        return code;
    }

    public UUID redeem(String code, String verifier) {
        PendingCode pending = code == null ? null : codes.remove(code);
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Código de login inválido ou expirado");
        }
        if (verifier == null || !VERIFIER.matcher(verifier).matches() || !matches(verifier, pending.challenge())) {
            throw new UnauthorizedException("Código de login inválido ou expirado");
        }
        return pending.userId();
    }

    private static boolean matches(String verifier, String challenge) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    challenge.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }

    private record PendingCode(UUID userId, String challenge, Instant expiresAt) {
    }
}
