package com.astra.roadmap.adapter.out.learning;

import com.astra.learning.application.port.in.CheckCourseOwnerQuery;
import com.astra.learning.application.port.in.ListCourseIdsQuery;
import com.astra.roadmap.application.port.out.UserCoursesPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class UserCoursesAdapter implements UserCoursesPort {

    private final CheckCourseOwnerQuery checkCourseOwnerQuery;
    private final ListCourseIdsQuery listCourseIdsQuery;

    UserCoursesAdapter(CheckCourseOwnerQuery checkCourseOwnerQuery, ListCourseIdsQuery listCourseIdsQuery) {
        this.checkCourseOwnerQuery = checkCourseOwnerQuery;
        this.listCourseIdsQuery = listCourseIdsQuery;
    }

    @Override
    public boolean isOwner(UUID courseId, UUID userId) {
        return checkCourseOwnerQuery.isOwner(courseId, userId);
    }

    @Override
    public List<UUID> courseIdsOf(UUID userId) {
        return listCourseIdsQuery.courseIdsOf(userId);
    }
}
