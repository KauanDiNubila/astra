package com.astra.learning.application.service;

import com.astra.learning.application.port.in.AddModuleCommand;
import com.astra.learning.application.port.in.AddModuleUseCase;
import com.astra.learning.application.port.in.ModuleResponse;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import com.astra.learning.domain.CourseModule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddModuleService implements AddModuleUseCase {

    private final LoadCoursePort loadCoursePort;
    private final UpdateCoursePort updateCoursePort;

    AddModuleService(LoadCoursePort loadCoursePort, UpdateCoursePort updateCoursePort) {
        this.loadCoursePort = loadCoursePort;
        this.updateCoursePort = updateCoursePort;
    }

    @Override
    @Transactional
    public ModuleResponse addModule(AddModuleCommand command) {
        Course course = OwnedCourse.load(loadCoursePort, command.userId(), command.courseId());
        CourseModule module = course.addModule(command.title(), command.position(), command.lessonCount());
        updateCoursePort.update(course);
        return ModuleResponse.from(module);
    }
}
