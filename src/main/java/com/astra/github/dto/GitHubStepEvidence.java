package com.astra.github.dto;

import java.util.List;

public record GitHubStepEvidence(boolean connected, List<GitHubRepositoryInsight> matchedRepositories) {
}
