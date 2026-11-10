package com.astra.github.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGitHubRepositoryRepository extends JpaRepository<GitHubRepositoryJpaEntity, UUID> {

    List<GitHubRepositoryJpaEntity> findByUserIdOrderByRecentCommitCountDesc(UUID userId);

    Optional<GitHubRepositoryJpaEntity> findByUserIdAndGithubRepoId(UUID userId, long githubRepoId);

    void deleteByUserId(UUID userId);
}
