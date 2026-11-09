package com.astra.tracking.session.adapter.out.learning;

import com.astra.learning.application.port.in.CheckCourseOwnerQuery;
import com.astra.tracking.session.application.port.out.CheckCourseOwnerPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CourseOwnerAdapter implements CheckCourseOwnerPort {

    private final CheckCourseOwnerQuery checkCourseOwnerQuery;

    CourseOwnerAdapter(CheckCourseOwnerQuery checkCourseOwnerQuery) {
        this.checkCourseOwnerQuery = checkCourseOwnerQuery;
    }

    @Override
    public boolean isOwner(UUID courseId, UUID userId) {
        return checkCourseOwnerQuery.isOwner(courseId, userId);
    }
}
