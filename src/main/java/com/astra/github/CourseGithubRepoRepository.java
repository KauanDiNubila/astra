package com.astra.github;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseGithubRepoRepository extends JpaRepository<CourseGithubRepo, CourseGithubRepoId> {

    List<CourseGithubRepo> findByCourseId(UUID courseId);

    void deleteByCourseIdAndRepositoryId(UUID courseId, UUID repositoryId);
}
