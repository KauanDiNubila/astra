package com.astra.learning.application.port.out;

import com.astra.learning.application.port.in.CourseRepositoriesResponse.LinkedRepository;
import java.util.List;
import java.util.UUID;

public interface LoadLinkedRepositoriesPort {

    List<LinkedRepository> repositories(List<UUID> repositoryIds);
}
