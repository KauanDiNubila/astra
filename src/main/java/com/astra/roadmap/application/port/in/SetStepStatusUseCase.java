package com.astra.roadmap.application.port.in;

import com.astra.roadmap.domain.StepStatus;
import java.util.UUID;

public interface SetStepStatusUseCase {

    StepResponse setStatus(UUID userId, UUID roadmapId, UUID stepId, StepStatus status);
}
