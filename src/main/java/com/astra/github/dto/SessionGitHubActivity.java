package com.astra.github.dto;

import java.util.List;
import java.util.UUID;

public record SessionGitHubActivity(
        boolean connected,
        List<GitHubCommitInfo> commits,
        UUID suggestedRepositoryId,
        String suggestedRepositoryName) {
}
