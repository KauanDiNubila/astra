package com.astra.learning.adapter.out.github;

import com.astra.github.entity.CourseGithubRepo;
import com.astra.github.repository.CourseGithubRepoRepository;
import com.astra.github.repository.GitHubRepositoryRepository;
import com.astra.learning.application.port.in.CourseRepositoriesResponse.LinkedRepository;
import com.astra.learning.application.port.out.CheckGithubRepositoryOwnerPort;
import com.astra.learning.application.port.out.CourseRepositoryLinksPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CourseRepositoryLinksAdapter implements CourseRepositoryLinksPort, CheckGithubRepositoryOwnerPort {

    private final CourseGithubRepoRepository courseGithubRepoRepository;
    private final GitHubRepositoryRepository gitHubRepositoryRepository;

    CourseRepositoryLinksAdapter(CourseGithubRepoRepository courseGithubRepoRepository,
                                 GitHubRepositoryRepository gitHubRepositoryRepository) {
        this.courseGithubRepoRepository = courseGithubRepoRepository;
        this.gitHubRepositoryRepository = gitHubRepositoryRepository;
    }

    @Override
    public boolean isOwner(UUID repositoryId, UUID userId) {
        return gitHubRepositoryRepository.findById(repositoryId)
                .filter(repository -> repository.getUserId().equals(userId))
                .isPresent();
    }

    @Override
    public List<LinkedRepository> repositoriesOf(UUID courseId) {
        return gitHubRepositoryRepository.findAllById(repositoryIdsOf(courseId)).stream()
                .map(r -> new LinkedRepository(r.getId(), r.getName(), r.getFullName(), r.getHtmlUrl(),
                        r.getPrimaryLanguage(), r.getRecentCommitCount(), r.isPrivate()))
                .toList();
    }

    @Override
    public List<UUID> repositoryIdsOf(UUID courseId) {
        return courseGithubRepoRepository.findByCourseId(courseId).stream()
                .map(CourseGithubRepo::getRepositoryId)
                .toList();
    }

    @Override
    public void link(UUID courseId, UUID repositoryId) {
        if (!repositoryIdsOf(courseId).contains(repositoryId)) {
            courseGithubRepoRepository.save(new CourseGithubRepo(courseId, repositoryId));
        }
    }

    @Override
    public void unlink(UUID courseId, UUID repositoryId) {
        courseGithubRepoRepository.deleteByCourseIdAndRepositoryId(courseId, repositoryId);
    }
}
