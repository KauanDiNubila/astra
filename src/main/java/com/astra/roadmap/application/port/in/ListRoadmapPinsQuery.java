package com.astra.roadmap.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ListRoadmapPinsQuery {

    Map<UUID, List<PinResponse>> pins(UUID userId, UUID roadmapId);
}
