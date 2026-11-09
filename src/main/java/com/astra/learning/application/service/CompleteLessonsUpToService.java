package com.astra.learning.application.service;

import com.astra.learning.application.port.in.CompleteLessonsUpToUseCase;
import com.astra.learning.application.port.in.ModuleResponse;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import com.astra.learning.domain.CourseModule;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CompleteLessonsUpToService implements CompleteLessonsUpToUseCase {

    private final LoadCoursePort loadCoursePort;
    private final UpdateCoursePort updateCoursePort;

    CompleteLessonsUpToService(LoadCoursePort loadCoursePort, UpdateCoursePort updateCoursePort) {
        this.loadCoursePort = loadCoursePort;
        this.updateCoursePort = updateCoursePort;
    }

    @Override
    @Transactional
    public ModuleResponse completeUpTo(UUID userId, UUID courseId, UUID moduleId, int position) {
        Course course = OwnedCourse.load(loadCoursePort, userId, courseId);
        CourseModule module = course.completeLessonsUpTo(moduleId, position);
        updateCoursePort.update(course);
        return ModuleResponse.from(module);
    }
}
