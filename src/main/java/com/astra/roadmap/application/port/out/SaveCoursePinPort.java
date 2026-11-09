package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.CoursePin;

public interface SaveCoursePinPort {

    void create(CoursePin pin);

    void update(CoursePin pin);
}
