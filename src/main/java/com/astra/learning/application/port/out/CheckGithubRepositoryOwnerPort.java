package com.astra.learning.application.port.out;

import java.util.UUID;

public interface CheckGithubRepositoryOwnerPort {

    boolean isOwner(UUID repositoryId, UUID userId);
}
