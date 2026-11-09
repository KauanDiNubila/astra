package com.astra.tracking.session.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Session {

    private final UUID id;
    private final UUID userId;
    private final UUID categoryId;
    private final UUID courseId;
    private UUID githubRepositoryId;
    private final int focusedMinutes;
    private final OffsetDateTime startedAt;
    private final String note;
    private final OffsetDateTime createdAt;

    private Session(UUID id, UUID userId, UUID categoryId, UUID courseId, UUID githubRepositoryId,
                    int focusedMinutes, OffsetDateTime startedAt, String note, OffsetDateTime createdAt) {
        if (focusedMinutes <= 0) {
            throw new IllegalArgumentException("focusedMinutes must be positive");
        }
        this.id = id;
        this.userId = Objects.requireNonNull(userId);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.courseId = courseId;
        this.githubRepositoryId = githubRepositoryId;
        this.focusedMinutes = focusedMinutes;
        this.startedAt = Objects.requireNonNull(startedAt);
        this.note = note;
        this.createdAt = createdAt;
    }

    public static Session start(UUID userId, UUID categoryId, UUID courseId, int focusedMinutes,
                                OffsetDateTime startedAt, String note) {
        return new Session(null, userId, categoryId, courseId, null, focusedMinutes, startedAt, note, null);
    }

    public static Session withId(UUID id, UUID userId, UUID categoryId, UUID courseId, UUID githubRepositoryId,
                                 int focusedMinutes, OffsetDateTime startedAt, String note, OffsetDateTime createdAt) {
        return new Session(Objects.requireNonNull(id), userId, categoryId, courseId, githubRepositoryId,
                focusedMinutes, startedAt, note, createdAt);
    }

    public boolean isOwnedBy(UUID userId) {
        return this.userId.equals(userId);
    }

    public OffsetDateTime endedAt() {
        return startedAt.plusMinutes(focusedMinutes);
    }

    public void linkRepository(UUID repositoryId) {
        this.githubRepositoryId = Objects.requireNonNull(repositoryId);
    }

    public void unlinkRepository() {
        this.githubRepositoryId = null;
    }
}
