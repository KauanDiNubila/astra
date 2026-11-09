package com.astra.roadmap.application.port.in;

import java.util.List;
import java.util.UUID;

public record StepEvidenceResponse(boolean connected, List<MatchedRepository> matchedRepositories) {

    public record MatchedRepository(
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
