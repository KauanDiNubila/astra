package com.astra.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astra.user.domain.RefreshToken.Rotation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionDomainTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-10T12:00:00Z");
    private static final UUID USER = UUID.randomUUID();

    private static RefreshToken fresh() {
        RefreshToken issued = RefreshToken.issue(USER, "raw", NOW, Duration.ofDays(30));
        return RefreshToken.withId(UUID.randomUUID(), USER, issued.getTokenHash(), issued.getExpiresAt(), null, false);
    }

    @Test
    void guardaSoOHashEValeOPrazo() {
        RefreshToken token = RefreshToken.issue(USER, "raw", NOW, Duration.ofDays(30));

        assertThat(token.getTokenHash()).isEqualTo(RefreshToken.hash("raw")).hasSize(64).doesNotContain("raw");
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plusDays(30));
    }

    @Test
    void rotacaoRevogaEMarcaComoRotacionado() {
        RefreshToken token = fresh();

        assertThat(token.rotate(NOW)).isEqualTo(Rotation.ROTATED);
        assertThat(token.getRevokedAt()).isEqualTo(NOW);
        assertThat(token.isRotated()).isTrue();
    }

    @Test
    void duasAbasDentroDaToleranciaPassamEDepoisViraReuso() {
        RefreshToken token = fresh();
        token.rotate(NOW);

        assertThat(token.rotate(NOW.plusSeconds(9))).isEqualTo(Rotation.CONCURRENT);
        assertThat(token.rotate(NOW.plusSeconds(11))).isEqualTo(Rotation.REUSED);
    }

    @Test
    void tokenRevogadoNoLogoutNaoTemTolerancia() {
        RefreshToken token = fresh();
        token.revoke(NOW);

        assertThat(token.rotate(NOW.plusSeconds(1))).isEqualTo(Rotation.REUSED);
    }

    @Test
    void tokenVencidoNaoRotaciona() {
        RefreshToken token = fresh();

        assertThat(token.rotate(NOW.plusDays(30))).isEqualTo(Rotation.EXPIRED);
        assertThat(token.getRevokedAt()).isNull();
    }

    @Test
    void codigoDoDesktopConfereOVerificadorPeloDesafio() throws Exception {
        String verifier = "v".repeat(43);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        DesktopLoginCode code = new DesktopLoginCode(USER, challenge, now.plusSeconds(120));

        assertThat(code.acceptsVerifier(verifier)).isTrue();
        assertThat(code.acceptsVerifier("w".repeat(43))).isFalse();
        assertThat(code.acceptsVerifier("curto")).isFalse();
        assertThat(code.acceptsVerifier(null)).isFalse();
        assertThat(code.isExpired(now.plusSeconds(119))).isFalse();
        assertThat(code.isExpired(now.plusSeconds(121))).isTrue();
        assertThat(DesktopLoginCode.isValidChallenge("curto")).isFalse();
        assertThatThrownBy(() -> new DesktopLoginCode(USER, "curto", now)).isInstanceOf(IllegalArgumentException.class);
    }
}
