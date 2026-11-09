package com.astra.roadmap.application.port.out;

import com.astra.roadmap.application.port.in.StepEvidenceResponse;
import java.util.UUID;

public interface GithubEvidencePort {

    StepEvidenceResponse evidenceFor(UUID userId, String stepTitle);
}
