package com.astra.learning.application.port.out;

import com.astra.learning.domain.Course;

public interface CreateCoursePort {

    void create(Course course);
}
