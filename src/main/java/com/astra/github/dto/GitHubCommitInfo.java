package com.astra.github.dto;

import java.time.OffsetDateTime;

public record GitHubCommitInfo(String sha, String message, OffsetDateTime committedAt, String repositoryFullName) {
}
