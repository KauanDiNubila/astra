package com.astra.github.application.port.in;

import java.util.UUID;

public interface GetStepEvidenceQuery {

    StepEvidenceResponse evidenceFor(UUID userId, String stepTitle);
}
