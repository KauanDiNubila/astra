package com.astra.roadmap.application.port.in;

import java.util.Map;
import java.util.UUID;

public interface ExportRoadmapsQuery {

    Map<String, Object> exportRoadmaps(UUID userId);
}
