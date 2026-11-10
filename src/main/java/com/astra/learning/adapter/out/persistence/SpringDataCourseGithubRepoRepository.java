package com.astra.learning.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCourseGithubRepoRepository
        extends JpaRepository<CourseGithubRepoJpaEntity, CourseGithubRepoJpaEntity.Key> {

    List<CourseGithubRepoJpaEntity> findByCourseId(UUID courseId);

    void deleteByCourseIdAndRepositoryId(UUID courseId, UUID repositoryId);
}
