package com.astra.github;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "github_connection")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "github_user_id", nullable = false)
    private long githubUserId;

    @Column(name = "github_login", nullable = false, length = 120)
    private String githubLogin;

    @Column(name = "github_avatar_url", length = 500)
    private String githubAvatarUrl;

    @Column(name = "access_token", nullable = false, columnDefinition = "text")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "text")
    private String refreshToken;

    @Column(name = "access_token_expires_at", nullable = false)
    private OffsetDateTime accessTokenExpiresAt;

    @Column(name = "refresh_token_expires_at")
    private OffsetDateTime refreshTokenExpiresAt;

    @Column(name = "connected_at", nullable = false)
    private OffsetDateTime connectedAt;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @Column(name = "last_sync_error", length = 300)
    private String lastSyncError;

    @Column(name = "visible_to_friends", nullable = false)
    private boolean visibleToFriends;

    public GitHubConnection(UUID userId, long githubUserId, String githubLogin, String githubAvatarUrl,
            String accessToken, String refreshToken, OffsetDateTime accessTokenExpiresAt,
            OffsetDateTime refreshTokenExpiresAt) {
        this.userId = userId;
        this.githubUserId = githubUserId;
        this.githubLogin = githubLogin;
        this.githubAvatarUrl = githubAvatarUrl;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.connectedAt = OffsetDateTime.now();
    }
}
