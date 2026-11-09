package com.astra.learning.application.port.in;

import java.util.UUID;

public interface GetCourseRepositoriesQuery {

    CourseRepositoriesResponse repositories(UUID userId, UUID courseId);
}
