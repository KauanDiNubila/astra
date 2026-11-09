package com.astra.learning.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, UUID> {

    Optional<CourseJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    List<CourseJpaEntity> findByUserId(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("select c.id from CourseJpaEntity c where c.userId = :userId")
    List<UUID> findIdsByUserId(@Param("userId") UUID userId);
}
