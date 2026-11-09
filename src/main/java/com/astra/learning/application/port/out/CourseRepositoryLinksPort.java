package com.astra.learning.application.port.out;

import com.astra.learning.application.port.in.CourseRepositoriesResponse.LinkedRepository;
import java.util.List;
import java.util.UUID;

public interface CourseRepositoryLinksPort {

    List<LinkedRepository> repositoriesOf(UUID courseId);

    List<UUID> repositoryIdsOf(UUID courseId);

    void link(UUID courseId, UUID repositoryId);

    void unlink(UUID courseId, UUID repositoryId);
}
