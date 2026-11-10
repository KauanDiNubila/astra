package com.astra.github.application.service;

import com.astra.github.application.port.in.ConnectGitHubUseCase;
import com.astra.github.application.port.in.GetConnectUrlQuery;
import com.astra.github.application.port.out.CreateConnectionPort;
import com.astra.github.application.port.out.BuildAuthorizeUrlPort;
import com.astra.github.application.port.out.ExchangeOAuthCodePort;
import com.astra.github.application.port.out.IssueConnectStatePort;
import com.astra.github.application.port.out.LoadGitHubProfilePort;
import com.astra.github.application.port.out.VerifyConnectStatePort;
import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.application.port.out.UpdateConnectionPort;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.GitHubProfile;
import com.astra.github.domain.GitHubTokens;
import com.astra.github.domain.TokenGrant;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ConnectGitHubService implements GetConnectUrlQuery, ConnectGitHubUseCase {

    private final IssueConnectStatePort issueConnectStatePort;
    private final VerifyConnectStatePort verifyConnectStatePort;
    private final BuildAuthorizeUrlPort buildAuthorizeUrlPort;
    private final ExchangeOAuthCodePort exchangeOAuthCodePort;
    private final LoadGitHubProfilePort loadGitHubProfilePort;
    private final LoadConnectionPort loadConnectionPort;
    private final CreateConnectionPort createConnectionPort;
    private final UpdateConnectionPort updateConnectionPort;
    private final Clock clock;

    ConnectGitHubService(IssueConnectStatePort issueConnectStatePort, VerifyConnectStatePort verifyConnectStatePort,
                         BuildAuthorizeUrlPort buildAuthorizeUrlPort, ExchangeOAuthCodePort exchangeOAuthCodePort,
                         LoadGitHubProfilePort loadGitHubProfilePort, LoadConnectionPort loadConnectionPort, CreateConnectionPort createConnectionPort,
                         UpdateConnectionPort updateConnectionPort, Clock clock) {
        this.issueConnectStatePort = issueConnectStatePort;
        this.verifyConnectStatePort = verifyConnectStatePort;
        this.buildAuthorizeUrlPort = buildAuthorizeUrlPort;
        this.exchangeOAuthCodePort = exchangeOAuthCodePort;
        this.loadGitHubProfilePort = loadGitHubProfilePort;
        this.loadConnectionPort = loadConnectionPort;
        this.createConnectionPort = createConnectionPort;
        this.updateConnectionPort = updateConnectionPort;
        this.clock = clock;
    }

    @Override
    public String connectUrl(UUID userId) {
        return buildAuthorizeUrlPort.authorizeUrl(issueConnectStatePort.issue(userId));
    }

    @Override
    @Transactional
    public UUID connect(String code, String state) {
        UUID userId = verifyConnectStatePort.verify(state);
        TokenGrant grant = exchangeOAuthCodePort.exchangeCode(code);
        GitHubProfile profile = loadGitHubProfilePort.viewer(grant.accessToken());
        OffsetDateTime now = OffsetDateTime.now(clock);
        GitHubTokens tokens = GitHubTokens.granted(grant, now);
        Optional<GitHubConnection> existing = loadConnectionPort.loadByUser(userId);
        if (existing.isPresent()) {
            existing.get().reconnect(profile, tokens);
            updateConnectionPort.update(existing.get());
        } else {
            createConnectionPort.create(GitHubConnection.connect(userId, profile, tokens, now));
        }
        return userId;
    }
}
