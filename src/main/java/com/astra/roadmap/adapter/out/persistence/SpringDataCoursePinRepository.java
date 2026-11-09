package com.astra.roadmap.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCoursePinRepository extends JpaRepository<CoursePinJpaEntity, UUID> {

    Optional<CoursePinJpaEntity> findByStepIdAndCourseId(UUID stepId, UUID courseId);

    List<CoursePinJpaEntity> findByStepIdInAndCourseIdIn(Collection<UUID> stepIds, Collection<UUID> courseIds);

    List<CoursePinJpaEntity> findByCourseIdIn(Collection<UUID> courseIds);
}
