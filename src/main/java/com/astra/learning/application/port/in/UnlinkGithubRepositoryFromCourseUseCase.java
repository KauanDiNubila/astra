package com.astra.learning.application.port.in;

import java.util.UUID;

public interface UnlinkGithubRepositoryFromCourseUseCase {

    void unlink(UUID userId, UUID courseId, UUID repositoryId);
}
