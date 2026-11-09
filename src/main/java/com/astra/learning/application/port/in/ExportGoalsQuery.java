package com.astra.learning.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExportGoalsQuery {

    List<Map<String, Object>> exportGoals(UUID userId);
}
