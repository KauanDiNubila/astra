package com.astra.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.ConflictException;
import com.astra.user.application.port.in.ResetPasswordCommand;
import com.astra.user.application.port.out.CheckPasswordBreachPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.application.port.out.PasswordResetTokenPort;
import com.astra.user.application.port.out.RevokeAllRefreshTokensPort;
import com.astra.user.application.port.out.SendPasswordResetEmailPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.PasswordResetToken;
import com.astra.user.domain.RefreshToken;
import com.astra.user.domain.Role;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PasswordResetServiceTest {

    private static final String NEW_PASSWORD = "Qz7#nB4tW1sK9pL2";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneOffset.UTC);
    private static final OffsetDateTime NOW = OffsetDateTime.now(CLOCK);

    private final LoadUserPort loadUserPort = mock(LoadUserPort.class);
    private final UpdateUserPort updateUserPort = mock(UpdateUserPort.class);
    private final PasswordHasherPort hasher = mock(PasswordHasherPort.class);
    private final CheckPasswordBreachPort breach = mock(CheckPasswordBreachPort.class);
    private final PasswordResetTokenPort tokens = mock(PasswordResetTokenPort.class);
    private final SendPasswordResetEmailPort email = mock(SendPasswordResetEmailPort.class);
    private final RevokeAllRefreshTokensPort revokeAll = mock(RevokeAllRefreshTokensPort.class);
    private final PasswordResetService service = new PasswordResetService(loadUserPort, updateUserPort, hasher, breach,
            tokens, email, revokeAll, CLOCK);

    private User stored(String passwordHash) {
        User user = User.withId(UUID.randomUUID(), "Ana", "ana@astra.local", passwordHash, null, Role.USER, null,
                "0001", null, null, NOW);
        when(loadUserPort.load(user.getId())).thenReturn(Optional.of(user));
        when(loadUserPort.loadByEmail(user.getEmail())).thenReturn(Optional.of(user));
        return user;
    }

    private PasswordResetToken storedToken(UUID userId, String rawToken, OffsetDateTime createdAt,
                                           OffsetDateTime usedAt) {
        PasswordResetToken token = PasswordResetToken.withId(UUID.randomUUID(), userId, RefreshToken.hash(rawToken),
                createdAt, createdAt.plus(PasswordResetToken.LIFETIME), usedAt);
        when(tokens.loadByHash(RefreshToken.hash(rawToken))).thenReturn(Optional.of(token));
        return token;
    }

    @Test
    void requestForUnknownEmailSendsNothing() {
        when(loadUserPort.loadByEmail("ninguem@astra.local")).thenReturn(Optional.empty());

        service.request("ninguem@astra.local");

        verify(tokens, never()).create(any());
        verify(email, never()).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void requestStoresOnlyTheHashAndEmailsTheRawToken() {
        User user = stored("hash");

        service.request(user.getEmail());

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(tokens).create(saved.capture());
        verify(email).sendPasswordReset(eq(user.getEmail()), sent.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo(RefreshToken.hash(sent.getValue()))
                .isNotEqualTo(sent.getValue());
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(PasswordResetToken.LIFETIME));
    }

    @Test
    void requestRightAfterAnotherSendsNothing() {
        User user = stored("hash");
        PasswordResetToken recent = PasswordResetToken.withId(UUID.randomUUID(), user.getId(), "h", NOW.minusSeconds(30),
                NOW.plusMinutes(29), null);
        when(tokens.loadLatestFor(user.getId())).thenReturn(Optional.of(recent));

        service.request(user.getEmail());

        verify(email, never()).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void resetSetsPasswordEvenForOAuthAccountAndRevokesSessions() {
        User user = stored(null);
        storedToken(user.getId(), "raw", NOW.minusMinutes(5), null);
        when(tokens.invalidateAllFor(user.getId(), NOW)).thenReturn(1);
        when(hasher.hash(NEW_PASSWORD)).thenReturn("new-hash");

        service.reset(new ResetPasswordCommand("raw", NEW_PASSWORD));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(updateUserPort).update(user);
        verify(revokeAll).revokeAllFor(user.getId(), NOW);
    }

    @Test
    void resetWithUnknownUsedOrExpiredTokenChangesNothing() {
        User user = stored("hash");
        storedToken(user.getId(), "used", NOW.minusMinutes(5), NOW.minusMinutes(1));
        storedToken(user.getId(), "expired", NOW.minus(PasswordResetToken.LIFETIME).minusSeconds(1), null);

        for (String raw : new String[] {"unknown", "used", "expired"}) {
            assertThatThrownBy(() -> service.reset(new ResetPasswordCommand(raw, NEW_PASSWORD)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(PasswordResetToken.INVALID_LINK);
        }
        verify(updateUserPort, never()).update(any());
        verify(revokeAll, never()).revokeAllFor(any(), any());
    }

    @Test
    void resetLosingTheRaceToAConcurrentResetChangesNothing() {
        User user = stored("hash");
        storedToken(user.getId(), "raw", NOW.minusMinutes(5), null);
        when(tokens.invalidateAllFor(user.getId(), NOW)).thenReturn(0);

        assertThatThrownBy(() -> service.reset(new ResetPasswordCommand("raw", NEW_PASSWORD)))
                .isInstanceOf(ConflictException.class);
        verify(updateUserPort, never()).update(any());
    }

    @Test
    void resetWithBreachedPasswordDoesNotBurnTheLink() {
        User user = stored("hash");
        storedToken(user.getId(), "raw", NOW.minusMinutes(5), null);
        when(breach.isBreached(NEW_PASSWORD)).thenReturn(true);

        assertThatThrownBy(() -> service.reset(new ResetPasswordCommand("raw", NEW_PASSWORD)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("vazamentos");
        verify(tokens, never()).invalidateAllFor(any(), any());
        verify(updateUserPort, never()).update(any());
    }
}
