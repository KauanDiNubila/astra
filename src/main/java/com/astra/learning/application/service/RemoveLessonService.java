package com.astra.learning.application.service;

import com.astra.learning.application.port.in.RemoveLessonUseCase;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RemoveLessonService implements RemoveLessonUseCase {

    private final LoadCoursePort loadCoursePort;
    private final UpdateCoursePort updateCoursePort;

    RemoveLessonService(LoadCoursePort loadCoursePort, UpdateCoursePort updateCoursePort) {
        this.loadCoursePort = loadCoursePort;
        this.updateCoursePort = updateCoursePort;
    }

    @Override
    @Transactional
    public void remove(UUID userId, UUID courseId, UUID moduleId, UUID lessonId) {
        Course course = OwnedCourse.load(loadCoursePort, userId, courseId);
        course.removeLesson(moduleId, lessonId);
        updateCoursePort.update(course);
    }
}
