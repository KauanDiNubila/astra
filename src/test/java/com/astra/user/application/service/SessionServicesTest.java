package com.astra.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.out.DesktopLoginCodeStorePort;
import com.astra.user.application.port.out.LoadRefreshTokenPort;
import com.astra.user.application.port.out.RevokeAllRefreshTokensPort;
import com.astra.user.application.port.out.UpdateRefreshTokenPort;
import com.astra.user.domain.DesktopLoginCode;
import com.astra.user.domain.RefreshToken;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionServicesTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
    private static final OffsetDateTime NOW = OffsetDateTime.now(CLOCK);
    private static final UUID USER = UUID.randomUUID();

    private final LoadRefreshTokenPort loadPort = mock(LoadRefreshTokenPort.class);
    private final UpdateRefreshTokenPort updatePort = mock(UpdateRefreshTokenPort.class);
    private final RevokeAllRefreshTokensPort revokeAllPort = mock(RevokeAllRefreshTokensPort.class);
    private final RotateRefreshTokenService rotateService =
            new RotateRefreshTokenService(loadPort, updatePort, revokeAllPort, CLOCK);

    private RefreshToken stored(OffsetDateTime revokedAt, boolean rotated, OffsetDateTime expiresAt) {
        RefreshToken token = RefreshToken.withId(UUID.randomUUID(), USER, RefreshToken.hash("raw"), expiresAt, revokedAt,
                rotated);
        when(loadPort.loadByHash(RefreshToken.hash("raw"))).thenReturn(Optional.of(token));
        return token;
    }

    @Test
    void rotacaoGravaERetornaODono() {
        RefreshToken token = stored(null, false, NOW.plusDays(1));

        assertThat(rotateService.rotate("raw")).isEqualTo(USER);
        verify(updatePort).update(token);
    }

    @Test
    void reusoDerrubaTodasAsSessoes() {
        stored(NOW.minusMinutes(1), true, NOW.plusDays(1));

        assertThatThrownBy(() -> rotateService.rotate("raw")).isInstanceOf(UnauthorizedException.class);
        verify(revokeAllPort).revokeAllFor(USER, NOW);
        verify(updatePort, never()).update(any());
    }

    @Test
    void concorrenteNaoGravaNemDerruba() {
        stored(NOW.minusSeconds(2), true, NOW.plusDays(1));

        assertThat(rotateService.rotate("raw")).isEqualTo(USER);
        verify(updatePort, never()).update(any());
        verify(revokeAllPort, never()).revokeAllFor(any(), any());
    }

    @Test
    void desconhecidoOuExpiradoRecusa() {
        assertThatThrownBy(() -> rotateService.rotate("outro")).hasMessage("Refresh token inválido");
        stored(null, false, NOW.minusSeconds(1));
        assertThatThrownBy(() -> rotateService.rotate("raw")).hasMessage("Refresh token expirado");
    }

    @Test
    void codigoDoDesktopSoVaiUmaVezEQueimaComVerificadorErrado() {
        DesktopLoginCodeStorePort store = mock(DesktopLoginCodeStorePort.class);
        RedeemDesktopLoginCodeService redeem = new RedeemDesktopLoginCodeService(store, CLOCK);
        when(store.take("code")).thenReturn(Optional.of(
                new DesktopLoginCode(USER, "c".repeat(43), CLOCK.instant().plusSeconds(60))));

        assertThatThrownBy(() -> redeem.redeem("code", "v".repeat(43))).isInstanceOf(UnauthorizedException.class);
        verify(store).take("code");
        assertThatThrownBy(() -> redeem.redeem(null, "v".repeat(43))).isInstanceOf(UnauthorizedException.class);
    }
}
