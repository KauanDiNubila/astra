package com.astra.github.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.github.entity.GitHubRepository;

public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, UUID> {

    List<GitHubRepository> findByUserIdOrderByRecentCommitCountDesc(UUID userId);

    Optional<GitHubRepository> findByUserIdAndGithubRepoId(UUID userId, long githubRepoId);

    void deleteByUserId(UUID userId);
}
