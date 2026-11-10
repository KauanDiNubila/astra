package com.astra.learning.adapter.out.persistence;

import com.astra.learning.application.port.out.CourseRepositoryLinksPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CourseRepositoryLinksPersistenceAdapter implements CourseRepositoryLinksPort {

    private final SpringDataCourseGithubRepoRepository linkRepository;

    CourseRepositoryLinksPersistenceAdapter(SpringDataCourseGithubRepoRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public List<UUID> repositoryIdsOf(UUID courseId) {
        return linkRepository.findByCourseId(courseId).stream()
                .map(CourseGithubRepoJpaEntity::getRepositoryId)
                .toList();
    }

    @Override
    public void link(UUID courseId, UUID repositoryId) {
        if (!repositoryIdsOf(courseId).contains(repositoryId)) {
            linkRepository.save(new CourseGithubRepoJpaEntity(courseId, repositoryId));
        }
    }

    @Override
    public void unlink(UUID courseId, UUID repositoryId) {
        linkRepository.deleteByCourseIdAndRepositoryId(courseId, repositoryId);
    }
}
