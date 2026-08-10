package com.astra.roadmap;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseStepLinkRepository extends JpaRepository<CourseStepLink, UUID> {

    Optional<CourseStepLink> findByStepIdAndCourseId(UUID stepId, UUID courseId);

    List<CourseStepLink> findByStepIdAndCourseIdIn(UUID stepId, Collection<UUID> courseIds);
}
