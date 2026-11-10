package com.astra.github.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SyncedRepository(UUID id, UUID userId, String owner, String name, String fullName, String description,
                               String primaryLanguage, String htmlUrl, boolean isPrivate, int starCount,
                               int forkCount, OffsetDateTime pushedAt, int recentCommitCount) {

    public boolean belongsTo(UUID userId) {
        return this.userId.equals(userId);
    }
}
