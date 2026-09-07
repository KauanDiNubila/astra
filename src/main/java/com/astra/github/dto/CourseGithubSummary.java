package com.astra.github.dto;

import java.util.List;

public record CourseGithubSummary(List<GitHubRepositoryInsight> repositories, int totalRecentCommits) {
}
