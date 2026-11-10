package com.astra.github.domain;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class GitHubConnection {

    public static final Duration SYNC_COOLDOWN = Duration.ofMinutes(2);
    public static final Duration REFRESH_SAFETY_MARGIN = Duration.ofMinutes(2);
    static final String INVALID_CONNECTION_ERROR = "Conexão inválida — reconecte sua conta do GitHub";

    private final UUID id;
    private final UUID userId;
    private GitHubProfile profile;
    private GitHubTokens tokens;
    private final OffsetDateTime connectedAt;
    private OffsetDateTime lastSyncedAt;
    private String lastSyncError;
    private boolean visibleToFriends;

    private GitHubConnection(UUID id, UUID userId, GitHubProfile profile, GitHubTokens tokens,
                             OffsetDateTime connectedAt, OffsetDateTime lastSyncedAt, String lastSyncError,
                             boolean visibleToFriends) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.profile = Objects.requireNonNull(profile);
        this.tokens = Objects.requireNonNull(tokens);
        this.connectedAt = connectedAt;
        this.lastSyncedAt = lastSyncedAt;
        this.lastSyncError = lastSyncError;
        this.visibleToFriends = visibleToFriends;
    }

    public static GitHubConnection connect(UUID userId, GitHubProfile profile, GitHubTokens tokens,
                                           OffsetDateTime now) {
        return new GitHubConnection(null, userId, profile, tokens, now, null, null, false);
    }

    public static GitHubConnection withId(UUID id, UUID userId, GitHubProfile profile, GitHubTokens tokens,
                                          OffsetDateTime connectedAt, OffsetDateTime lastSyncedAt,
                                          String lastSyncError, boolean visibleToFriends) {
        return new GitHubConnection(Objects.requireNonNull(id), userId, profile, tokens, connectedAt, lastSyncedAt,
                lastSyncError, visibleToFriends);
    }

    public void reconnect(GitHubProfile profile, GitHubTokens tokens) {
        this.profile = Objects.requireNonNull(profile);
        this.tokens = Objects.requireNonNull(tokens);
        this.lastSyncError = null;
    }

    public void checkCanSync(OffsetDateTime now) {
        if (lastSyncedAt != null && lastSyncedAt.isAfter(now.minus(SYNC_COOLDOWN))) {
            throw new SyncTooSoonException();
        }
    }

    public String accessToken(OffsetDateTime now, TokenRefresher refresher) {
        if (!tokens.readable()) {
            lastSyncError = INVALID_CONNECTION_ERROR;
            throw new InvalidGitHubConnectionException();
        }
        boolean expiringSoon = tokens.accessExpiresAt().isBefore(now.plus(REFRESH_SAFETY_MARGIN));
        if (!expiringSoon || tokens.refreshToken() == null) {
            return tokens.accessToken();
        }
        tokens = GitHubTokens.granted(refresher.refresh(tokens.refreshToken()), now);
        return tokens.accessToken();
    }

    public void recordSync(OffsetDateTime at, String error) {
        this.lastSyncedAt = at;
        this.lastSyncError = error;
    }

    public void changeVisibility(boolean visibleToFriends) {
        this.visibleToFriends = visibleToFriends;
    }
}
