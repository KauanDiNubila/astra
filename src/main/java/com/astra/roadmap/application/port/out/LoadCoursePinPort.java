package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.CoursePin;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadCoursePinPort {

    Optional<CoursePin> load(UUID pinId);

    Optional<CoursePin> loadOn(UUID stepId, UUID courseId);

    List<CoursePin> listOn(Collection<UUID> stepIds, Collection<UUID> courseIds);

    List<CoursePin> listForCourses(Collection<UUID> courseIds);
}
