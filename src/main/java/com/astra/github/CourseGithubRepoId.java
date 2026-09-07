package com.astra.github;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CourseGithubRepoId implements Serializable {

    private UUID courseId;
    private UUID repositoryId;

    public CourseGithubRepoId() {
    }

    public CourseGithubRepoId(UUID courseId, UUID repositoryId) {
        this.courseId = courseId;
        this.repositoryId = repositoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseGithubRepoId that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId) && Objects.equals(repositoryId, that.repositoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, repositoryId);
    }
}
