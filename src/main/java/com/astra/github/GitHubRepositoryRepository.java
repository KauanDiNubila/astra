package com.astra.github;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, UUID> {

    List<GitHubRepository> findByUserIdOrderByRecentCommitCountDesc(UUID userId);

    Optional<GitHubRepository> findByUserIdAndGithubRepoId(UUID userId, long githubRepoId);

    void deleteByUserId(UUID userId);
}
