package com.astra.learning.application.service;

import com.astra.learning.application.port.in.CompleteLessonUseCase;
import com.astra.learning.application.port.in.LessonResponse;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import com.astra.learning.domain.Lesson;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CompleteLessonService implements CompleteLessonUseCase {

    private final LoadCoursePort loadCoursePort;
    private final UpdateCoursePort updateCoursePort;

    CompleteLessonService(LoadCoursePort loadCoursePort, UpdateCoursePort updateCoursePort) {
        this.loadCoursePort = loadCoursePort;
        this.updateCoursePort = updateCoursePort;
    }

    @Override
    @Transactional
    public LessonResponse markCompleted(UUID userId, UUID courseId, UUID moduleId, UUID lessonId,
                                        boolean completed) {
        Course course = OwnedCourse.load(loadCoursePort, userId, courseId);
        Lesson lesson = course.markLessonCompleted(moduleId, lessonId, completed);
        updateCoursePort.update(course);
        return LessonResponse.from(lesson);
    }
}
