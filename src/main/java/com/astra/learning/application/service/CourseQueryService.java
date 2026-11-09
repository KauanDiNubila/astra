package com.astra.learning.application.service;

import com.astra.learning.application.port.in.CheckCourseOwnerQuery;
import com.astra.learning.application.port.in.CourseDetailResponse;
import com.astra.learning.application.port.in.CourseRepositoriesResponse;
import com.astra.learning.application.port.in.CourseResponse;
import com.astra.learning.application.port.in.ExportCoursesQuery;
import com.astra.learning.application.port.in.GetCourseQuery;
import com.astra.learning.application.port.in.GetCourseRepositoriesQuery;
import com.astra.learning.application.port.in.ListCourseIdsQuery;
import com.astra.learning.application.port.in.ListCoursesQuery;
import com.astra.learning.application.port.out.CheckCourseOwnershipPort;
import com.astra.learning.application.port.out.CourseRepositoryLinksPort;
import com.astra.learning.application.port.out.ListCoursesPort;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.LoadLastStudiedPort;
import com.astra.learning.domain.Course;
import com.astra.learning.domain.CourseModule;
import com.astra.shared.ExportRow;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CourseQueryService implements ListCoursesQuery, GetCourseQuery, GetCourseRepositoriesQuery,
        CheckCourseOwnerQuery, ListCourseIdsQuery, ExportCoursesQuery {

    private final ListCoursesPort listCoursesPort;
    private final LoadCoursePort loadCoursePort;
    private final LoadLastStudiedPort loadLastStudiedPort;
    private final CheckCourseOwnershipPort checkCourseOwnershipPort;
    private final CourseRepositoryLinksPort courseRepositoryLinksPort;

    CourseQueryService(ListCoursesPort listCoursesPort, LoadCoursePort loadCoursePort,
                       LoadLastStudiedPort loadLastStudiedPort, CheckCourseOwnershipPort checkCourseOwnershipPort,
                       CourseRepositoryLinksPort courseRepositoryLinksPort) {
        this.listCoursesPort = listCoursesPort;
        this.loadCoursePort = loadCoursePort;
        this.loadLastStudiedPort = loadLastStudiedPort;
        this.checkCourseOwnershipPort = checkCourseOwnershipPort;
        this.courseRepositoryLinksPort = courseRepositoryLinksPort;
    }

    @Override
    public List<CourseResponse> list(UUID userId) {
        List<Course> courses = listCoursesPort.listByUser(userId);
        if (courses.isEmpty()) {
            return List.of();
        }
        Map<UUID, OffsetDateTime> lastStudied = loadLastStudiedPort.byCourse(
                userId, courses.stream().map(Course::getId).toList());
        return courses.stream()
                .map(course -> CourseResponse.from(course, lastStudied.get(course.getId())))
                .toList();
    }

    @Override
    public CourseDetailResponse get(UUID userId, UUID courseId) {
        return CourseDetailResponse.from(OwnedCourse.load(loadCoursePort, userId, courseId));
    }

    @Override
    public CourseRepositoriesResponse repositories(UUID userId, UUID courseId) {
        OwnedCourse.load(loadCoursePort, userId, courseId);
        return CourseRepositoriesResponse.of(courseRepositoryLinksPort.repositoriesOf(courseId));
    }

    @Override
    public boolean isOwner(UUID courseId, UUID userId) {
        return checkCourseOwnershipPort.isOwner(courseId, userId);
    }

    @Override
    public List<UUID> courseIdsOf(UUID userId) {
        return checkCourseOwnershipPort.courseIdsOf(userId);
    }

    @Override
    public List<Map<String, Object>> exportCourses(UUID userId) {
        return listCoursesPort.listByUser(userId).stream()
                .map(course -> ExportRow.of("id", course.getId(), "title", course.getTitle(),
                        "platform", course.getPlatform(), "status", course.getStatus(),
                        "modules", course.getModules().stream().map(CourseQueryService::exportModule).toList(),
                        "githubRepositoryIds", courseRepositoryLinksPort.repositoryIdsOf(course.getId())))
                .toList();
    }

    private static Map<String, Object> exportModule(CourseModule module) {
        return ExportRow.of("id", module.getId(), "title", module.getTitle(), "position", module.getPosition(),
                "lessons", module.getLessons().stream()
                        .map(lesson -> ExportRow.of("title", lesson.getTitle(), "position", lesson.getPosition(),
                                "completed", lesson.isCompleted()))
                        .toList());
    }
}
