package com.astra.github;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class GitHubActivitySummaryId implements Serializable {

    private UUID userId;
    private ActivityPeriod period;

    public GitHubActivitySummaryId() {
    }

    public GitHubActivitySummaryId(UUID userId, ActivityPeriod period) {
        this.userId = userId;
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitHubActivitySummaryId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && period == that.period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, period);
    }
}
