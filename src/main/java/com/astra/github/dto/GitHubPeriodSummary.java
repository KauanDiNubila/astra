package com.astra.github.dto;

import com.astra.github.ActivityPeriod;

public record GitHubPeriodSummary(
        ActivityPeriod period,
        int commitCount,
        int pullRequestOpenedCount,
        int pullRequestMergedCount,
        int issueClosedCount,
        int activeRepoCount) {
}
