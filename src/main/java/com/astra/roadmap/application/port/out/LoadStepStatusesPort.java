package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.StepProgress;
import com.astra.roadmap.domain.StepStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LoadStepStatusesPort {

    Map<UUID, StepStatus> statusesOf(UUID userId, Collection<UUID> stepIds);

    List<StepProgress> listByUser(UUID userId);
}
