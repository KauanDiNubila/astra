package com.astra.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, UUID> {

    Optional<OAuthConnection> findByProviderAndProviderUserId(String provider, String providerUserId);
}
