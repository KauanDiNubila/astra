package com.astra.tracking.session.adapter.out.learning;

import com.astra.learning.service.CourseService;
import com.astra.tracking.session.application.port.out.CheckCourseOwnerPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CourseOwnerAdapter implements CheckCourseOwnerPort {

    private final CourseService courseService;

    CourseOwnerAdapter(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public boolean isOwner(UUID courseId, UUID userId) {
        return courseService.existsForUser(courseId, userId);
    }
}
