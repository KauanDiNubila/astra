package com.astra.learning.application.service;

import com.astra.learning.application.port.in.ModuleResponse;
import com.astra.learning.application.port.in.UpdateModuleCommand;
import com.astra.learning.application.port.in.UpdateModuleUseCase;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateModuleService implements UpdateModuleUseCase {

    private final LoadCoursePort loadCoursePort;
    private final UpdateCoursePort updateCoursePort;

    UpdateModuleService(LoadCoursePort loadCoursePort, UpdateCoursePort updateCoursePort) {
        this.loadCoursePort = loadCoursePort;
        this.updateCoursePort = updateCoursePort;
    }

    @Override
    @Transactional
    public ModuleResponse update(UpdateModuleCommand command) {
        Course course = OwnedCourse.load(loadCoursePort, command.userId(), command.courseId());
        course.module(command.moduleId());
        if (command.title() != null) {
            course.renameModule(command.moduleId(), command.title());
        }
        if (command.completed() != null) {
            course.markModuleCompleted(command.moduleId(), command.completed());
        }
        updateCoursePort.update(course);
        return ModuleResponse.from(course.module(command.moduleId()));
    }
}
