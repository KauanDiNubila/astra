package com.astra.learning.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "course_github_repo")
@IdClass(CourseGithubRepoJpaEntity.Key.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class CourseGithubRepoJpaEntity {

    @Id
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Id
    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @CreationTimestamp
    @Column(name = "linked_at", nullable = false)
    private OffsetDateTime linkedAt;

    CourseGithubRepoJpaEntity(UUID courseId, UUID repositoryId) {
        this.courseId = courseId;
        this.repositoryId = repositoryId;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    static class Key implements Serializable {

        private UUID courseId;
        private UUID repositoryId;
    }
}
