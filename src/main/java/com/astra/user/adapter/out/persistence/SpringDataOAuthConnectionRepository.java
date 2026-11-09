package com.astra.user.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOAuthConnectionRepository extends JpaRepository<OAuthConnectionJpaEntity, UUID> {

    Optional<OAuthConnectionJpaEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<OAuthConnectionJpaEntity> findByUserId(UUID userId);
}
