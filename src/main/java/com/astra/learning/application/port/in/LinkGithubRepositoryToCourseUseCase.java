package com.astra.learning.application.port.in;

import java.util.UUID;

public interface LinkGithubRepositoryToCourseUseCase {

    void link(UUID userId, UUID courseId, UUID repositoryId);
}
