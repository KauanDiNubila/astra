package com.astra.github;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubDailyActivityRepository extends JpaRepository<GitHubDailyActivity, GitHubDailyActivityId> {

    List<GitHubDailyActivity> findByUserIdAndActivityDateGreaterThanEqualOrderByActivityDate(
            UUID userId, LocalDate since);

    Optional<GitHubDailyActivity> findByUserIdAndActivityDate(UUID userId, LocalDate activityDate);

    void deleteByUserId(UUID userId);
}
