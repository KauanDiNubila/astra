package com.astra.learning.application.port.out;

import java.util.List;
import java.util.UUID;

public interface CourseRepositoryLinksPort {

    List<UUID> repositoryIdsOf(UUID courseId);

    void link(UUID courseId, UUID repositoryId);

    void unlink(UUID courseId, UUID repositoryId);
}
