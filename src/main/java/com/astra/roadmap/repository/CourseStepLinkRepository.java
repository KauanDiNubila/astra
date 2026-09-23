package com.astra.roadmap.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.roadmap.entity.CourseStepLink;

public interface CourseStepLinkRepository extends JpaRepository<CourseStepLink, UUID> {

    Optional<CourseStepLink> findByStepIdAndCourseId(UUID stepId, UUID courseId);

    List<CourseStepLink> findByStepIdInAndCourseIdIn(Collection<UUID> stepIds, Collection<UUID> courseIds);
}
