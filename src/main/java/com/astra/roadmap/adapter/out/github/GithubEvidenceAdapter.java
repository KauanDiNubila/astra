package com.astra.roadmap.adapter.out.github;

import com.astra.github.application.port.in.GetStepEvidenceQuery;
import com.astra.roadmap.application.port.in.StepEvidenceResponse;
import com.astra.roadmap.application.port.in.StepEvidenceResponse.MatchedRepository;
import com.astra.roadmap.application.port.out.GithubEvidencePort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GithubEvidenceAdapter implements GithubEvidencePort {

    private final GetStepEvidenceQuery getStepEvidenceQuery;

    GithubEvidenceAdapter(GetStepEvidenceQuery getStepEvidenceQuery) {
        this.getStepEvidenceQuery = getStepEvidenceQuery;
    }

    @Override
    public StepEvidenceResponse evidenceFor(UUID userId, String stepTitle) {
        var evidence = getStepEvidenceQuery.evidenceFor(userId, stepTitle);
        return new StepEvidenceResponse(evidence.connected(), evidence.matchedRepositories().stream()
                .map(r -> new MatchedRepository(r.id(), r.name(), r.fullName(), r.htmlUrl(), r.primaryLanguage(),
                        r.recentCommitCount(), r.isPrivate()))
                .toList());
    }
}
