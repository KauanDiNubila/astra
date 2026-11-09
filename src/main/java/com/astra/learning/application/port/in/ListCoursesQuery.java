package com.astra.learning.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListCoursesQuery {

    List<CourseResponse> list(UUID userId);
}
