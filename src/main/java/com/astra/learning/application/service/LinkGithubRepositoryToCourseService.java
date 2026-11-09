package com.astra.learning.application.service;

import com.astra.learning.application.port.in.LinkGithubRepositoryToCourseUseCase;
import com.astra.learning.application.port.out.CheckGithubRepositoryOwnerPort;
import com.astra.learning.application.port.out.CourseRepositoryLinksPort;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.domain.Course;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class LinkGithubRepositoryToCourseService implements LinkGithubRepositoryToCourseUseCase {

    private final LoadCoursePort loadCoursePort;
    private final CheckGithubRepositoryOwnerPort checkGithubRepositoryOwnerPort;
    private final CourseRepositoryLinksPort courseRepositoryLinksPort;

    LinkGithubRepositoryToCourseService(LoadCoursePort loadCoursePort, CheckGithubRepositoryOwnerPort checkGithubRepositoryOwnerPort, CourseRepositoryLinksPort courseRepositoryLinksPort) {
        this.loadCoursePort = loadCoursePort;
        this.checkGithubRepositoryOwnerPort = checkGithubRepositoryOwnerPort;
        this.courseRepositoryLinksPort = courseRepositoryLinksPort;
    }

    @Override
    @Transactional
    public void link(UUID userId, UUID courseId, UUID repositoryId) {
        Course course = OwnedCourse.load(loadCoursePort, userId, courseId);
        if (!checkGithubRepositoryOwnerPort.isOwner(repositoryId, course.getUserId())) {
            throw new NotFoundException("Repositório não encontrado");
        }
        courseRepositoryLinksPort.link(courseId, repositoryId);
    }
}
