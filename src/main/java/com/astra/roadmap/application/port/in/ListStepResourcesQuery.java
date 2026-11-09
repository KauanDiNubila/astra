package com.astra.roadmap.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListStepResourcesQuery {

    List<ResourceResponse> list(UUID userId, UUID stepId);
}
