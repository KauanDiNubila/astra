package com.astra.learning.application.service;

import com.astra.learning.application.port.in.UnlinkGithubRepositoryFromCourseUseCase;
import com.astra.learning.application.port.out.CourseRepositoryLinksPort;
import com.astra.learning.application.port.out.LoadCoursePort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UnlinkGithubRepositoryFromCourseService implements UnlinkGithubRepositoryFromCourseUseCase {

    private final LoadCoursePort loadCoursePort;
    private final CourseRepositoryLinksPort courseRepositoryLinksPort;

    UnlinkGithubRepositoryFromCourseService(LoadCoursePort loadCoursePort, CourseRepositoryLinksPort courseRepositoryLinksPort) {
        this.loadCoursePort = loadCoursePort;
        this.courseRepositoryLinksPort = courseRepositoryLinksPort;
    }

    @Override
    @Transactional
    public void unlink(UUID userId, UUID courseId, UUID repositoryId) {
        OwnedCourse.load(loadCoursePort, userId, courseId);
        courseRepositoryLinksPort.unlink(courseId, repositoryId);
    }
}
