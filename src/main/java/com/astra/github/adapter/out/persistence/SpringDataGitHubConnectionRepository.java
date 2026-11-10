package com.astra.github.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGitHubConnectionRepository extends JpaRepository<GitHubConnectionJpaEntity, UUID> {

    Optional<GitHubConnectionJpaEntity> findByUserId(UUID userId);

    List<GitHubConnectionJpaEntity> findByUserIdIn(Collection<UUID> userIds);

    void deleteByUserId(UUID userId);
}
