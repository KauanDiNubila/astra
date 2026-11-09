package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.GetStepGithubEvidenceQuery;
import com.astra.roadmap.application.port.in.ListStepResourcesQuery;
import com.astra.roadmap.application.port.in.ResourceResponse;
import com.astra.roadmap.application.port.in.StepEvidenceResponse;
import com.astra.roadmap.application.port.out.GithubEvidencePort;
import com.astra.roadmap.application.port.out.LoadStepPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class StepQueryService implements ListStepResourcesQuery, GetStepGithubEvidenceQuery {

    private final LoadStepPort loadStepPort;
    private final GithubEvidencePort githubEvidencePort;

    StepQueryService(LoadStepPort loadStepPort, GithubEvidencePort githubEvidencePort) {
        this.loadStepPort = loadStepPort;
        this.githubEvidencePort = githubEvidencePort;
    }

    @Override
    public List<ResourceResponse> list(UUID userId, UUID stepId) {
        return Accessible.step(loadStepPort, userId, stepId).step().getResources().stream()
                .map(ResourceResponse::from)
                .toList();
    }

    @Override
    public StepEvidenceResponse evidence(UUID userId, UUID stepId) {
        return githubEvidencePort.evidenceFor(userId, Accessible.step(loadStepPort, userId, stepId).step().getTitle());
    }
}
