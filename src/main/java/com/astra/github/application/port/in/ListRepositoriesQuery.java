package com.astra.github.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ListRepositoriesQuery {

    List<RepositoryInsight> byIds(Collection<UUID> repositoryIds);
}
