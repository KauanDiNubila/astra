package com.astra.github.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGitHubActivitySummaryRepository
        extends JpaRepository<GitHubActivitySummaryJpaEntity, GitHubActivitySummaryJpaEntity.Key> {

    List<GitHubActivitySummaryJpaEntity> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
