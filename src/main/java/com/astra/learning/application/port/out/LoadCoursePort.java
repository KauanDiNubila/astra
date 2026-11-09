package com.astra.learning.application.port.out;

import com.astra.learning.domain.Course;
import java.util.Optional;
import java.util.UUID;

public interface LoadCoursePort {

    Optional<Course> loadOwned(UUID courseId, UUID userId);
}
