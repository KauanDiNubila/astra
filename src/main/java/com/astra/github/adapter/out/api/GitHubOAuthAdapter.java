package com.astra.github.adapter.out.api;

import com.astra.github.application.port.out.BuildAuthorizeUrlPort;
import com.astra.github.application.port.out.ExchangeOAuthCodePort;
import com.astra.github.application.port.out.LoadGitHubProfilePort;
import com.astra.github.application.port.out.RefreshAccessTokenPort;
import com.astra.github.domain.GitHubProfile;
import com.astra.github.domain.TokenGrant;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class GitHubOAuthAdapter implements BuildAuthorizeUrlPort, ExchangeOAuthCodePort, RefreshAccessTokenPort,
        LoadGitHubProfilePort {

    private final GitHubApiClient apiClient;
    private final String clientId;
    private final String redirectUri;

    GitHubOAuthAdapter(GitHubApiClient apiClient,
                       @Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId,
                       @Value("${astra.github.connect-redirect-uri}") String redirectUri) {
        this.apiClient = apiClient;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    @Override
    public String authorizeUrl(String state) {
        return "https://github.com/login/oauth/authorize?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=public_repo"
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }

    @Override
    public TokenGrant exchangeCode(String code) {
        return apiClient.exchangeCode(code, redirectUri);
    }

    @Override
    public TokenGrant refresh(String refreshToken) {
        return apiClient.refreshToken(refreshToken);
    }

    @Override
    public GitHubProfile viewer(String accessToken) {
        Map<String, Object> profile = apiClient.fetchViewerProfile(accessToken);
        return new GitHubProfile(((Number) profile.get("id")).longValue(), (String) profile.get("login"),
                (String) profile.get("avatar_url"));
    }
}
