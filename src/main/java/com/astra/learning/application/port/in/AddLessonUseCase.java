package com.astra.learning.application.port.in;

public interface AddLessonUseCase {

    LessonResponse addLesson(AddLessonCommand command);
}
