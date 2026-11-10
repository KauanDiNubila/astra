package com.astra.github.adapter.out.persistence;

import com.astra.github.application.port.out.LoadSyncedDataPort;
import com.astra.github.application.port.out.RecordSyncedDataPort;
import com.astra.github.domain.DailyContribution;
import com.astra.github.domain.PeriodSummary;
import com.astra.github.domain.RepositorySnapshot;
import com.astra.github.domain.SyncedRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class SyncedDataPersistenceAdapter implements RecordSyncedDataPort, LoadSyncedDataPort {

    private final SpringDataGitHubRepositoryRepository repositoryRepository;
    private final SpringDataGitHubDailyActivityRepository dailyActivityRepository;
    private final SpringDataGitHubActivitySummaryRepository activitySummaryRepository;

    SyncedDataPersistenceAdapter(SpringDataGitHubRepositoryRepository repositoryRepository,
                                 SpringDataGitHubDailyActivityRepository dailyActivityRepository,
                                 SpringDataGitHubActivitySummaryRepository activitySummaryRepository) {
        this.repositoryRepository = repositoryRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.activitySummaryRepository = activitySummaryRepository;
    }

    @Override
    public void recordDaily(UUID userId, List<DailyContribution> days) {
        dailyActivityRepository.saveAll(days.stream()
                .map(d -> new GitHubDailyActivityJpaEntity(userId, d.date(), d.count()))
                .toList());
    }

    @Override
    public void recordRepositories(UUID userId, List<RepositorySnapshot> repositories, OffsetDateTime syncedAt) {
        for (RepositorySnapshot snapshot : repositories) {
            GitHubRepositoryJpaEntity entity = repositoryRepository
                    .findByUserIdAndGithubRepoId(userId, snapshot.githubRepoId())
                    .orElseGet(() -> newRepository(userId, snapshot));
            entity.setDescription(snapshot.description());
            entity.setPrimaryLanguage(snapshot.primaryLanguage());
            entity.setStarCount(snapshot.starCount());
            entity.setForkCount(snapshot.forkCount());
            entity.setPushedAt(snapshot.pushedAt());
            entity.setRecentCommitCount(snapshot.recentCommitCount());
            entity.setSyncedAt(syncedAt);
            repositoryRepository.save(entity);
        }
    }

    @Override
    public void recordSummary(UUID userId, PeriodSummary s) {
        activitySummaryRepository.save(new GitHubActivitySummaryJpaEntity(userId, s.period(), s.commitCount(),
                s.pullRequestOpenedCount(), s.pullRequestMergedCount(), s.issueClosedCount(), s.activeRepoCount()));
    }

    @Override
    public List<SyncedRepository> repositoriesByActivity(UUID userId) {
        return repositoryRepository.findByUserIdOrderByRecentCommitCountDesc(userId).stream()
                .map(SyncedDataPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<SyncedRepository> repository(UUID repositoryId) {
        return repositoryRepository.findById(repositoryId).map(SyncedDataPersistenceAdapter::toDomain);
    }

    @Override
    public List<SyncedRepository> repositories(Collection<UUID> repositoryIds) {
        return repositoryRepository.findAllById(repositoryIds).stream()
                .map(SyncedDataPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<DailyContribution> dailySince(UUID userId, LocalDate since) {
        return dailyActivityRepository.findByUserIdAndActivityDateGreaterThanEqualOrderByActivityDate(userId, since)
                .stream()
                .map(d -> new DailyContribution(d.getActivityDate(), d.getContributionCount()))
                .toList();
    }

    @Override
    public List<DailyContribution> allDaily(UUID userId) {
        return dailyActivityRepository.findByUserIdOrderByActivityDate(userId).stream()
                .map(d -> new DailyContribution(d.getActivityDate(), d.getContributionCount()))
                .toList();
    }

    @Override
    public List<PeriodSummary> summaries(UUID userId) {
        return activitySummaryRepository.findByUserId(userId).stream()
                .map(s -> new PeriodSummary(s.getPeriod(), s.getCommitCount(), s.getPullRequestOpenedCount(),
                        s.getPullRequestMergedCount(), s.getIssueClosedCount(), s.getActiveRepoCount()))
                .toList();
    }

    private static GitHubRepositoryJpaEntity newRepository(UUID userId, RepositorySnapshot snapshot) {
        GitHubRepositoryJpaEntity entity = new GitHubRepositoryJpaEntity(userId, snapshot.githubRepoId());
        entity.setOwner(snapshot.owner());
        entity.setName(snapshot.name());
        entity.setFullName(snapshot.fullName());
        entity.setHtmlUrl(snapshot.htmlUrl());
        entity.setPrivate(snapshot.isPrivate());
        entity.setDefaultBranch(snapshot.defaultBranch());
        entity.setCreatedAtOnGithub(snapshot.createdAtOnGithub());
        return entity;
    }

    private static SyncedRepository toDomain(GitHubRepositoryJpaEntity r) {
        return new SyncedRepository(r.getId(), r.getUserId(), r.getOwner(), r.getName(), r.getFullName(),
                r.getDescription(), r.getPrimaryLanguage(), r.getHtmlUrl(), r.isPrivate(), r.getStarCount(),
                r.getForkCount(), r.getPushedAt(), r.getRecentCommitCount());
    }
}
