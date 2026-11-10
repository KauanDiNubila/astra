package com.astra.github.domain;

public record PeriodSummary(ActivityPeriod period, int commitCount, int pullRequestOpenedCount,
                            int pullRequestMergedCount, int issueClosedCount, int activeRepoCount) {
}
