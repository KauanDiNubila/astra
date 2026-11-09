package com.astra.roadmap.application.port.in;

import java.util.UUID;

public interface GetRoadmapQuery {

    RoadmapDetailResponse get(UUID userId, UUID roadmapId);
}
