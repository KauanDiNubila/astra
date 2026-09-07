package com.astra.github;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "github_activity_summary")
@IdClass(GitHubActivitySummaryId.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubActivitySummary {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ActivityPeriod period;

    @Column(name = "commit_count", nullable = false)
    private int commitCount;

    @Column(name = "pull_request_opened_count", nullable = false)
    private int pullRequestOpenedCount;

    @Column(name = "pull_request_merged_count", nullable = false)
    private int pullRequestMergedCount;

    @Column(name = "issue_closed_count", nullable = false)
    private int issueClosedCount;

    @Column(name = "active_repo_count", nullable = false)
    private int activeRepoCount;

    public GitHubActivitySummary(UUID userId, ActivityPeriod period, int commitCount, int pullRequestOpenedCount,
            int pullRequestMergedCount, int issueClosedCount, int activeRepoCount) {
        this.userId = userId;
        this.period = period;
        this.commitCount = commitCount;
        this.pullRequestOpenedCount = pullRequestOpenedCount;
        this.pullRequestMergedCount = pullRequestMergedCount;
        this.issueClosedCount = issueClosedCount;
        this.activeRepoCount = activeRepoCount;
    }
}
