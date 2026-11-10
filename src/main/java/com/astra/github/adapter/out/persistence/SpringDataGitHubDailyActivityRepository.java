package com.astra.github.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface SpringDataGitHubDailyActivityRepository
        extends JpaRepository<GitHubDailyActivityJpaEntity, GitHubDailyActivityJpaEntity.Key> {

    List<GitHubDailyActivityJpaEntity> findByUserIdAndActivityDateGreaterThanEqualOrderByActivityDate(
            UUID userId, LocalDate since);

    List<GitHubDailyActivityJpaEntity> findByUserIdOrderByActivityDate(UUID userId);

    void deleteByUserId(UUID userId);
}
