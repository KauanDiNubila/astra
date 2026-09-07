package com.astra.github;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubActivitySummaryRepository extends JpaRepository<GitHubActivitySummary, GitHubActivitySummaryId> {

    List<GitHubActivitySummary> findByUserId(UUID userId);

    Optional<GitHubActivitySummary> findByUserIdAndPeriod(UUID userId, ActivityPeriod period);

    void deleteByUserId(UUID userId);
}
