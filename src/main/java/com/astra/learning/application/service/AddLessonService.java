package com.astra.learning.application.service;

import com.astra.learning.application.port.in.AddLessonCommand;
import com.astra.learning.application.port.in.AddLessonUseCase;
import com.astra.learning.application.port.in.LessonResponse;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.Course;
import com.astra.learning.domain.Lesson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddLessonService implements AddLessonUseCase {

    private final LoadCoursePort loadCoursePort;
    private final UpdateCoursePort updateCoursePort;

    AddLessonService(LoadCoursePort loadCoursePort, UpdateCoursePort updateCoursePort) {
        this.loadCoursePort = loadCoursePort;
        this.updateCoursePort = updateCoursePort;
    }

    @Override
    @Transactional
    public LessonResponse addLesson(AddLessonCommand command) {
        Course course = OwnedCourse.load(loadCoursePort, command.userId(), command.courseId());
        Lesson lesson = course.addLesson(command.moduleId(), command.title(), command.position());
        updateCoursePort.update(course);
        return LessonResponse.from(lesson);
    }
}
