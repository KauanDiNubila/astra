package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.LocatedStep;
import java.util.Optional;
import java.util.UUID;

public interface LoadStepPort {

    Optional<LocatedStep> loadStep(UUID stepId);
}
