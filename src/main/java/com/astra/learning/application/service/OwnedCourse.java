package com.astra.learning.application.service;

import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.domain.Course;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;

final class OwnedCourse {

    private OwnedCourse() {
    }

    static Course load(LoadCoursePort loadCoursePort, UUID userId, UUID courseId) {
        return loadCoursePort.loadOwned(courseId, userId)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }
}
