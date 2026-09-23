package com.astra.github.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.github.entity.GitHubConnection;

public interface GitHubConnectionRepository extends JpaRepository<GitHubConnection, UUID> {

    Optional<GitHubConnection> findByUserId(UUID userId);

    List<GitHubConnection> findByUserIdIn(Collection<UUID> userIds);

    void deleteByUserId(UUID userId);
}
