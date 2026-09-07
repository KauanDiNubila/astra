package com.astra.github;

import com.astra.github.dto.GitHubConnectionStatus;
import com.astra.github.dto.GitHubTokenResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.crypto.EncryptionService;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.shared.security.JwtService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GitHubConnectionService {

    private static final String CONNECT_PURPOSE = "github_connect";
    // Renova um pouco antes de vencer de verdade, pra nao correr risco de o
    // token expirar no meio de uma sequencia de chamadas de um sync.
    private static final Duration REFRESH_SAFETY_MARGIN = Duration.ofMinutes(2);

    private final GitHubConnectionRepository connectionRepository;
    private final GitHubRepositoryRepository repositoryRepository;
    private final GitHubDailyActivityRepository dailyActivityRepository;
    private final GitHubActivitySummaryRepository activitySummaryRepository;
    private final GitHubApiClient apiClient;
    private final EncryptionService encryptionService;
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;
    private final String clientId;
    private final String redirectUri;

    public GitHubConnectionService(GitHubConnectionRepository connectionRepository,
            GitHubRepositoryRepository repositoryRepository,
            GitHubDailyActivityRepository dailyActivityRepository,
            GitHubActivitySummaryRepository activitySummaryRepository,
            GitHubApiClient apiClient, EncryptionService encryptionService, JwtService jwtService,
            CurrentUserProvider currentUserProvider,
            @Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId,
            @Value("${astra.github.connect-redirect-uri}") String redirectUri) {
        this.connectionRepository = connectionRepository;
        this.repositoryRepository = repositoryRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.activitySummaryRepository = activitySummaryRepository;
        this.apiClient = apiClient;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.currentUserProvider = currentUserProvider;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public String buildAuthorizeUrl() {
        UUID userId = currentUserProvider.currentUserId();
        String state = jwtService.generatePurposeToken(userId, CONNECT_PURPOSE, Duration.ofMinutes(5));
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
        return "https://github.com/login/oauth/authorize?client_id=" + clientId
                + "&redirect_uri=" + encodedRedirect
                + "&scope=public_repo"
                + "&state=" + encodedState;
    }

    @Transactional
    public UUID handleCallback(String code, String state) {
        UUID userId = jwtService.extractPurposeUserId(state, CONNECT_PURPOSE);

        GitHubTokenResponse token = apiClient.exchangeCode(code, redirectUri);
        Map<String, Object> profile = apiClient.fetchViewerProfile(token.accessToken());

        long githubUserId = ((Number) profile.get("id")).longValue();
        String login = (String) profile.get("login");
        String avatarUrl = (String) profile.get("avatar_url");
        OffsetDateTime now = OffsetDateTime.now();

        GitHubConnection connection = connectionRepository.findByUserId(userId).orElse(null);
        if (connection == null) {
            connection = new GitHubConnection(userId, githubUserId, login, avatarUrl,
                    encryptionService.encrypt(token.accessToken()),
                    encryptionService.encrypt(token.refreshToken()),
                    now.plusSeconds(token.expiresInSeconds()),
                    token.refreshTokenExpiresInSeconds() == null
                            ? null : now.plusSeconds(token.refreshTokenExpiresInSeconds()));
        } else {
            connection.setGithubUserId(githubUserId);
            connection.setGithubLogin(login);
            connection.setGithubAvatarUrl(avatarUrl);
            connection.setAccessToken(encryptionService.encrypt(token.accessToken()));
            connection.setRefreshToken(encryptionService.encrypt(token.refreshToken()));
            connection.setAccessTokenExpiresAt(now.plusSeconds(token.expiresInSeconds()));
            connection.setRefreshTokenExpiresAt(token.refreshTokenExpiresInSeconds() == null
                    ? null : now.plusSeconds(token.refreshTokenExpiresInSeconds()));
            connection.setLastSyncError(null);
        }
        connectionRepository.save(connection);
        return userId;
    }

    @Transactional(readOnly = true)
    public GitHubConnectionStatus status() {
        UUID userId = currentUserProvider.currentUserId();
        return connectionRepository.findByUserId(userId)
                .map(c -> new GitHubConnectionStatus(true, c.getGithubLogin(), c.getGithubAvatarUrl(),
                        c.getConnectedAt(), c.getLastSyncedAt(), c.getLastSyncError(), c.isVisibleToFriends()))
                .orElseGet(GitHubConnectionStatus::disconnected);
    }

    @Transactional
    public void updateVisibility(boolean visibleToFriends) {
        UUID userId = currentUserProvider.currentUserId();
        GitHubConnection connection = connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("GitHub não conectado"));
        connection.setVisibleToFriends(visibleToFriends);
    }

    @Transactional
    public void disconnect() {
        UUID userId = currentUserProvider.currentUserId();
        dailyActivityRepository.deleteByUserId(userId);
        activitySummaryRepository.deleteByUserId(userId);
        repositoryRepository.deleteByUserId(userId);
        connectionRepository.deleteByUserId(userId);
    }

    @Transactional
    public String validAccessToken(UUID userId) {
        GitHubConnection connection = connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("GitHub não conectado"));
        return validAccessToken(connection);
    }

    private String validAccessToken(GitHubConnection connection) {
        String accessToken;
        try {
            accessToken = encryptionService.decryptStrict(connection.getAccessToken());
        } catch (RuntimeException corrupted) {
            connection.setLastSyncError("Conexão inválida — reconecte sua conta do GitHub");
            connectionRepository.save(connection);
            throw new ConflictException("Conexão com o GitHub inválida — reconecte");
        }

        boolean expiringSoon = connection.getAccessTokenExpiresAt()
                .isBefore(OffsetDateTime.now().plus(REFRESH_SAFETY_MARGIN));
        if (!expiringSoon) {
            return accessToken;
        }
        if (connection.getRefreshToken() == null) {
            return accessToken;
        }

        String refreshToken = encryptionService.decryptStrict(connection.getRefreshToken());
        GitHubTokenResponse refreshed = apiClient.refreshToken(refreshToken);
        OffsetDateTime now = OffsetDateTime.now();
        connection.setAccessToken(encryptionService.encrypt(refreshed.accessToken()));
        connection.setRefreshToken(encryptionService.encrypt(refreshed.refreshToken()));
        connection.setAccessTokenExpiresAt(now.plusSeconds(refreshed.expiresInSeconds()));
        connection.setRefreshTokenExpiresAt(refreshed.refreshTokenExpiresInSeconds() == null
                ? null : now.plusSeconds(refreshed.refreshTokenExpiresInSeconds()));
        connectionRepository.save(connection);
        return refreshed.accessToken();
    }

    @Transactional(readOnly = true)
    public GitHubConnection requireConnection(UUID userId) {
        return connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("GitHub não conectado"));
    }

    @Transactional
    public void markSyncResult(UUID userId, OffsetDateTime syncedAt, String error) {
        connectionRepository.findByUserId(userId).ifPresent(c -> {
            c.setLastSyncedAt(syncedAt);
            c.setLastSyncError(error);
        });
    }
}
