package com.astra.github.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.github.application.port.out.RefreshAccessTokenPort;
import com.astra.github.application.port.out.UpdateConnectionPort;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.GitHubProfile;
import com.astra.github.domain.GitHubTokens;
import com.astra.github.domain.InvalidGitHubConnectionException;
import com.astra.github.domain.TokenGrant;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccessTokensTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-10T12:00:00Z");

    private final RefreshAccessTokenPort refreshPort = mock(RefreshAccessTokenPort.class);
    private final UpdateConnectionPort updatePort = mock(UpdateConnectionPort.class);
    private final AccessTokens accessTokens = new AccessTokens(refreshPort, updatePort,
            Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC));

    private static GitHubConnection connection(GitHubTokens tokens) {
        return GitHubConnection.withId(UUID.randomUUID(), UUID.randomUUID(), new GitHubProfile(1, "octocat", null),
                tokens, NOW, null, null, false);
    }

    @Test
    void tokenValidoNaoGravaNada() {
        GitHubConnection connection = connection(new GitHubTokens("a", "r", NOW.plusHours(1), null, true));

        assertThat(accessTokens.validFor(connection)).isEqualTo("a");
        verify(updatePort, never()).update(connection);
    }

    @Test
    void tokenRenovadoEGravado() {
        GitHubConnection connection = connection(new GitHubTokens("a", "r", NOW.plusMinutes(1), null, true));
        when(refreshPort.refresh("r")).thenReturn(new TokenGrant("novo", "r2", 28800, null));

        assertThat(accessTokens.validFor(connection)).isEqualTo("novo");
        verify(updatePort).update(connection);
    }

    @Test
    void conexaoInvalidaGravaOErroAntesDeFalhar() {
        GitHubConnection connection = connection(GitHubTokens.unreadable(NOW, null));

        assertThatThrownBy(() -> accessTokens.validFor(connection)).isInstanceOf(InvalidGitHubConnectionException.class);
        verify(updatePort).update(connection);
    }
}
