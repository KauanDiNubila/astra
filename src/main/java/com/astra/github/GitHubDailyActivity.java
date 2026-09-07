package com.astra.github;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "github_daily_activity")
@IdClass(GitHubDailyActivityId.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubDailyActivity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "contribution_count", nullable = false)
    private int contributionCount;

    public GitHubDailyActivity(UUID userId, LocalDate activityDate, int contributionCount) {
        this.userId = userId;
        this.activityDate = activityDate;
        this.contributionCount = contributionCount;
    }
}
