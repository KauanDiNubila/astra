package com.astra.learning.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExportCoursesQuery {

    List<Map<String, Object>> exportCourses(UUID userId);
}
