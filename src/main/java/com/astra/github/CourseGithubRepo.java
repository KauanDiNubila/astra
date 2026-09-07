package com.astra.github;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "course_github_repo")
@IdClass(CourseGithubRepoId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseGithubRepo {

    @Id
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Id
    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @CreationTimestamp
    @Column(name = "linked_at", nullable = false)
    private OffsetDateTime linkedAt;

    public CourseGithubRepo(UUID courseId, UUID repositoryId) {
        this.courseId = courseId;
        this.repositoryId = repositoryId;
    }
}
