package com.astra.github.adapter.out.persistence;

import com.astra.github.application.port.out.CreateConnectionPort;
import com.astra.github.application.port.out.DeleteGitHubDataPort;
import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.application.port.out.UpdateConnectionPort;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.GitHubProfile;
import com.astra.github.domain.GitHubTokens;
import com.astra.shared.crypto.EncryptionService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ConnectionPersistenceAdapter implements LoadConnectionPort, CreateConnectionPort, UpdateConnectionPort,
        DeleteGitHubDataPort {

    private final SpringDataGitHubConnectionRepository connectionRepository;
    private final SpringDataGitHubRepositoryRepository repositoryRepository;
    private final SpringDataGitHubDailyActivityRepository dailyActivityRepository;
    private final SpringDataGitHubActivitySummaryRepository activitySummaryRepository;
    private final EncryptionService encryptionService;

    ConnectionPersistenceAdapter(SpringDataGitHubConnectionRepository connectionRepository,
                                 SpringDataGitHubRepositoryRepository repositoryRepository,
                                 SpringDataGitHubDailyActivityRepository dailyActivityRepository,
                                 SpringDataGitHubActivitySummaryRepository activitySummaryRepository,
                                 EncryptionService encryptionService) {
        this.connectionRepository = connectionRepository;
        this.repositoryRepository = repositoryRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.activitySummaryRepository = activitySummaryRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public Optional<GitHubConnection> loadByUser(UUID userId) {
        return connectionRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<GitHubConnection> loadByUsers(Collection<UUID> userIds) {
        return connectionRepository.findByUserIdIn(userIds).stream().map(this::toDomain).toList();
    }

    @Override
    public void create(GitHubConnection connection) {
        GitHubConnectionJpaEntity entity =
                new GitHubConnectionJpaEntity(connection.getUserId(), connection.getConnectedAt());
        copy(connection, entity);
        connectionRepository.save(entity);
    }

    @Override
    public void update(GitHubConnection connection) {
        GitHubConnectionJpaEntity entity = connectionRepository.findById(connection.getId()).orElseThrow();
        copy(connection, entity);
        connectionRepository.save(entity);
    }

    @Override
    public void deleteAllFor(UUID userId) {
        dailyActivityRepository.deleteByUserId(userId);
        activitySummaryRepository.deleteByUserId(userId);
        repositoryRepository.deleteByUserId(userId);
        connectionRepository.deleteByUserId(userId);
    }

    private void copy(GitHubConnection connection, GitHubConnectionJpaEntity entity) {
        entity.setGithubUserId(connection.getProfile().githubUserId());
        entity.setGithubLogin(connection.getProfile().login());
        entity.setGithubAvatarUrl(connection.getProfile().avatarUrl());
        GitHubTokens tokens = connection.getTokens();
        if (tokens.readable()) {
            entity.setAccessToken(encryptionService.encrypt(tokens.accessToken()));
            entity.setRefreshToken(encryptionService.encrypt(tokens.refreshToken()));
            entity.setAccessTokenExpiresAt(tokens.accessExpiresAt());
            entity.setRefreshTokenExpiresAt(tokens.refreshExpiresAt());
        }
        entity.setLastSyncedAt(connection.getLastSyncedAt());
        entity.setLastSyncError(connection.getLastSyncError());
        entity.setVisibleToFriends(connection.isVisibleToFriends());
    }

    private GitHubConnection toDomain(GitHubConnectionJpaEntity entity) {
        return GitHubConnection.withId(entity.getId(), entity.getUserId(),
                new GitHubProfile(entity.getGithubUserId(), entity.getGithubLogin(), entity.getGithubAvatarUrl()),
                tokensOf(entity), entity.getConnectedAt(), entity.getLastSyncedAt(), entity.getLastSyncError(),
                entity.isVisibleToFriends());
    }

    private GitHubTokens tokensOf(GitHubConnectionJpaEntity entity) {
        try {
            return new GitHubTokens(encryptionService.decryptStrict(entity.getAccessToken()),
                    entity.getRefreshToken() == null ? null : encryptionService.decryptStrict(entity.getRefreshToken()),
                    entity.getAccessTokenExpiresAt(), entity.getRefreshTokenExpiresAt(), true);
        } catch (RuntimeException unreadable) {
            return GitHubTokens.unreadable(entity.getAccessTokenExpiresAt(), entity.getRefreshTokenExpiresAt());
        }
    }
}
