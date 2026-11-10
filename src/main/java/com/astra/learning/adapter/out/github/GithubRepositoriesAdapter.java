package com.astra.learning.adapter.out.github;

import com.astra.github.application.port.in.CheckRepositoryOwnerQuery;
import com.astra.github.application.port.in.ListRepositoriesQuery;
import com.astra.learning.application.port.in.CourseRepositoriesResponse.LinkedRepository;
import com.astra.learning.application.port.out.CheckGithubRepositoryOwnerPort;
import com.astra.learning.application.port.out.LoadLinkedRepositoriesPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GithubRepositoriesAdapter implements LoadLinkedRepositoriesPort, CheckGithubRepositoryOwnerPort {

    private final ListRepositoriesQuery listRepositoriesQuery;
    private final CheckRepositoryOwnerQuery checkRepositoryOwnerQuery;

    GithubRepositoriesAdapter(ListRepositoriesQuery listRepositoriesQuery,
                              CheckRepositoryOwnerQuery checkRepositoryOwnerQuery) {
        this.listRepositoriesQuery = listRepositoriesQuery;
        this.checkRepositoryOwnerQuery = checkRepositoryOwnerQuery;
    }

    @Override
    public boolean isOwner(UUID repositoryId, UUID userId) {
        return checkRepositoryOwnerQuery.isOwner(repositoryId, userId);
    }

    @Override
    public List<LinkedRepository> repositories(List<UUID> repositoryIds) {
        return listRepositoriesQuery.byIds(repositoryIds).stream()
                .map(r -> new LinkedRepository(r.id(), r.name(), r.fullName(), r.htmlUrl(), r.primaryLanguage(),
                        r.recentCommitCount(), r.isPrivate()))
                .toList();
    }
}
