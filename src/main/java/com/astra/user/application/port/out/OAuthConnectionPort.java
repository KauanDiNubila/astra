package com.astra.user.application.port.out;

import com.astra.user.domain.OAuthConnection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuthConnectionPort {

    Optional<UUID> findUserId(String provider, String providerUserId);

    void create(OAuthConnection connection);

    List<OAuthConnection> listByUser(UUID userId);
}
