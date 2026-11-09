package com.astra.learning.application.port.out;

import com.astra.learning.domain.Course;
import java.util.List;
import java.util.UUID;

public interface ListCoursesPort {

    List<Course> listByUser(UUID userId);
}
