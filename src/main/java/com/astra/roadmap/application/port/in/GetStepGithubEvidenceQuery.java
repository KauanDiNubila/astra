package com.astra.roadmap.application.port.in;

import java.util.UUID;

public interface GetStepGithubEvidenceQuery {

    StepEvidenceResponse evidence(UUID userId, UUID stepId);
}
