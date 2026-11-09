package com.astra.roadmap.application.port.in;

import java.util.UUID;

public interface RemoveStepResourceUseCase {

    void remove(UUID userId, UUID stepId, UUID resourceId);
}
