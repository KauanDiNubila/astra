package com.astra.github;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "github_repository")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "github_repo_id", nullable = false)
    private long githubRepoId;

    @Column(nullable = false, length = 120)
    private String owner;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "full_name", nullable = false, length = 320)
    private String fullName;

    @Column(length = 500)
    private String description;

    @Column(name = "primary_language", length = 60)
    private String primaryLanguage;

    @Column(name = "html_url", nullable = false, length = 500)
    private String htmlUrl;

    @Column(name = "private", nullable = false)
    private boolean isPrivate;

    @Column(name = "star_count", nullable = false)
    private int starCount;

    @Column(name = "fork_count", nullable = false)
    private int forkCount;

    @Column(name = "default_branch", length = 100)
    private String defaultBranch;

    @Column(name = "pushed_at")
    private OffsetDateTime pushedAt;

    @Column(name = "created_at_on_github")
    private OffsetDateTime createdAtOnGithub;

    @Column(name = "recent_commit_count", nullable = false)
    private int recentCommitCount;

    @Column(name = "synced_at", nullable = false)
    private OffsetDateTime syncedAt;

    public GitHubRepository(UUID userId, long githubRepoId, String owner, String name, String fullName,
            String description, String primaryLanguage, String htmlUrl, boolean isPrivate, int starCount,
            int forkCount, String defaultBranch, OffsetDateTime pushedAt, OffsetDateTime createdAtOnGithub) {
        this.userId = userId;
        this.githubRepoId = githubRepoId;
        this.owner = owner;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.primaryLanguage = primaryLanguage;
        this.htmlUrl = htmlUrl;
        this.isPrivate = isPrivate;
        this.starCount = starCount;
        this.forkCount = forkCount;
        this.defaultBranch = defaultBranch;
        this.pushedAt = pushedAt;
        this.createdAtOnGithub = createdAtOnGithub;
        this.syncedAt = OffsetDateTime.now();
    }
}
