package com.astra.github.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.github.entity.CourseGithubRepo;
import com.astra.github.entity.CourseGithubRepoId;

public interface CourseGithubRepoRepository extends JpaRepository<CourseGithubRepo, CourseGithubRepoId> {

    List<CourseGithubRepo> findByCourseId(UUID courseId);

    void deleteByCourseIdAndRepositoryId(UUID courseId, UUID repositoryId);
}
