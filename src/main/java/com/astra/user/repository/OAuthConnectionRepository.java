package com.astra.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.user.entity.OAuthConnection;

public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, UUID> {

    Optional<OAuthConnection> findByProviderAndProviderUserId(String provider, String providerUserId);
}
