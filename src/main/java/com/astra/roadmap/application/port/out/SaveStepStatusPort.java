package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.StepStatus;
import java.util.UUID;

public interface SaveStepStatusPort {

    void save(UUID userId, UUID stepId, StepStatus status);
}
