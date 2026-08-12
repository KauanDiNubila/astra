package com.astra.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

    List<CourseModule> findByCourseIdOrderByPosition(UUID courseId);

    Optional<CourseModule> findByIdAndCourseId(UUID id, UUID courseId);

    long countByCourseId(UUID courseId);
}
