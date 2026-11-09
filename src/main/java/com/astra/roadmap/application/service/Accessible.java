package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.out.LoadStepPort;
import com.astra.roadmap.domain.LocatedStep;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;

final class Accessible {

    private Accessible() {
    }

    static LocatedStep step(LoadStepPort loadStepPort, UUID userId, UUID stepId) {
        return loadStepPort.loadStep(stepId)
                .filter(step -> step.isAccessibleBy(userId))
                .orElseThrow(() -> new NotFoundException("Step not found"));
    }
}
