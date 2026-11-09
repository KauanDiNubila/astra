package com.astra.learning.application.port.in;

import java.util.List;
import java.util.UUID;

public record CourseRepositoriesResponse(List<LinkedRepository> repositories, int totalRecentCommits) {

    public static CourseRepositoriesResponse of(List<LinkedRepository> repositories) {
        return new CourseRepositoriesResponse(repositories,
                repositories.stream().mapToInt(LinkedRepository::recentCommitCount).sum());
    }

    public record LinkedRepository(
            UUID id,
            String name,
            String fullName,
            String htmlUrl,
            String primaryLanguage,
            int recentCommitCount,
            boolean isPrivate
    ) {
    }
}
