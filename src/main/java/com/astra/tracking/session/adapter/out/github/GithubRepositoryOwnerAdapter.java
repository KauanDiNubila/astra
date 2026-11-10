package com.astra.tracking.session.adapter.out.github;

import com.astra.github.application.port.in.CheckRepositoryOwnerQuery;
import com.astra.tracking.session.application.port.out.CheckGithubRepositoryOwnerPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class GithubRepositoryOwnerAdapter implements CheckGithubRepositoryOwnerPort {

    private final CheckRepositoryOwnerQuery checkRepositoryOwnerQuery;

    GithubRepositoryOwnerAdapter(CheckRepositoryOwnerQuery checkRepositoryOwnerQuery) {
        this.checkRepositoryOwnerQuery = checkRepositoryOwnerQuery;
    }

    @Override
    public boolean isOwner(UUID repositoryId, UUID userId) {
        return checkRepositoryOwnerQuery.isOwner(repositoryId, userId);
    }
}
