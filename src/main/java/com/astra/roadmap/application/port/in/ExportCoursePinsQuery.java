package com.astra.roadmap.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExportCoursePinsQuery {

    List<Map<String, Object>> exportPins(UUID userId);
}
