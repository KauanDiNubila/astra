package com.astra.roadmap.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListRoadmapsQuery {

    List<RoadmapResponse> list(UUID userId);
}
