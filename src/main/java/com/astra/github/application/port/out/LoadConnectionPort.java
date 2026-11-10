package com.astra.github.application.port.out;

import com.astra.github.domain.GitHubConnection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadConnectionPort {

    Optional<GitHubConnection> loadByUser(UUID userId);

    List<GitHubConnection> loadByUsers(Collection<UUID> userIds);
}
