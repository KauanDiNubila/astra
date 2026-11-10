package com.astra.github.domain;

import java.time.OffsetDateTime;

public record RepositorySnapshot(long githubRepoId, String owner, String name, String fullName, String description,
                                 String primaryLanguage, String htmlUrl, boolean isPrivate, int starCount,
                                 int forkCount, String defaultBranch, OffsetDateTime pushedAt,
                                 OffsetDateTime createdAtOnGithub, int recentCommitCount) {
}
