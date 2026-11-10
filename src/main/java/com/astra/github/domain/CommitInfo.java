package com.astra.github.domain;

import java.time.OffsetDateTime;

public record CommitInfo(String sha, String message, OffsetDateTime committedAt, String repositoryFullName) {
}
