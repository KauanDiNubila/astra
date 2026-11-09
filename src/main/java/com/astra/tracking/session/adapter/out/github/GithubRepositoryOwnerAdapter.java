package com.astra.tracking.session.adapter.out.github;

import com.astra.github.repository.GitHubRepositoryRepository;
import com.astra.tracking.session.application.port.out.CheckGithubRepositoryOwnerPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GithubRepositoryOwnerAdapter implements CheckGithubRepositoryOwnerPort {

    private final GitHubRepositoryRepository gitHubRepositoryRepository;

    GithubRepositoryOwnerAdapter(GitHubRepositoryRepository gitHubRepositoryRepository) {
        this.gitHubRepositoryRepository = gitHubRepositoryRepository;
    }

    @Override
    public boolean isOwner(UUID repositoryId, UUID userId) {
        return gitHubRepositoryRepository.findById(repositoryId)
                .filter(repository -> repository.getUserId().equals(userId))
                .isPresent();
    }
}
