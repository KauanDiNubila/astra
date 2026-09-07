package com.astra.github.dto;

public record GitHubRepositoryInsight(
        java.util.UUID id,
        String name,
        String fullName,
        String htmlUrl,
        String primaryLanguage,
        int recentCommitCount,
        boolean isPrivate) {
}
