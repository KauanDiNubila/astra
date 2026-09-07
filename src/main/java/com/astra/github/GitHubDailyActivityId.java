package com.astra.github;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class GitHubDailyActivityId implements Serializable {

    private UUID userId;
    private LocalDate activityDate;

    public GitHubDailyActivityId() {
    }

    public GitHubDailyActivityId(UUID userId, LocalDate activityDate) {
        this.userId = userId;
        this.activityDate = activityDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitHubDailyActivityId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(activityDate, that.activityDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, activityDate);
    }
}
