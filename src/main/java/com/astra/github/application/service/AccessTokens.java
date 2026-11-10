package com.astra.github.application.service;

import com.astra.github.application.port.out.RefreshAccessTokenPort;
import com.astra.github.application.port.out.UpdateConnectionPort;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.GitHubTokens;
import com.astra.github.domain.InvalidGitHubConnectionException;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
class AccessTokens {

    private final RefreshAccessTokenPort refreshAccessTokenPort;
    private final UpdateConnectionPort updateConnectionPort;
    private final Clock clock;

    AccessTokens(RefreshAccessTokenPort refreshAccessTokenPort, UpdateConnectionPort updateConnectionPort, Clock clock) {
        this.refreshAccessTokenPort = refreshAccessTokenPort;
        this.updateConnectionPort = updateConnectionPort;
        this.clock = clock;
    }

    String validFor(GitHubConnection connection) {
        GitHubTokens before = connection.getTokens();
        try {
            String accessToken = connection.accessToken(OffsetDateTime.now(clock), refreshAccessTokenPort::refresh);
            if (connection.getTokens() != before) {
                updateConnectionPort.update(connection);
            }
            return accessToken;
        } catch (InvalidGitHubConnectionException invalid) {
            updateConnectionPort.update(connection);
            throw invalid;
        }
    }
}
