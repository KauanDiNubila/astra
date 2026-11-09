package com.astra.learning.application.port.in;

public interface CreateCourseUseCase {

    CourseResponse create(CreateCourseCommand command);
}
