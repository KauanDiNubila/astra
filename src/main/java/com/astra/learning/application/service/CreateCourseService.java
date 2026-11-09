package com.astra.learning.application.service;

import com.astra.learning.application.port.in.CourseResponse;
import com.astra.learning.application.port.in.CreateCourseCommand;
import com.astra.learning.application.port.in.CreateCourseUseCase;
import com.astra.learning.application.port.out.CreateCoursePort;
import com.astra.learning.domain.Course;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCourseService implements CreateCourseUseCase {

    private final CreateCoursePort createCoursePort;

    CreateCourseService(CreateCoursePort createCoursePort) {
        this.createCoursePort = createCoursePort;
    }

    @Override
    @Transactional
    public CourseResponse create(CreateCourseCommand command) {
        Course course = Course.plan(command.userId(), command.title(), command.platform(), command.moduleCount());
        createCoursePort.create(course);
        return CourseResponse.from(course, null);
    }
}
