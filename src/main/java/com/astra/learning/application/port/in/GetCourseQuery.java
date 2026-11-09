package com.astra.learning.application.port.in;

import java.util.UUID;

public interface GetCourseQuery {

    CourseDetailResponse get(UUID userId, UUID courseId);
}
