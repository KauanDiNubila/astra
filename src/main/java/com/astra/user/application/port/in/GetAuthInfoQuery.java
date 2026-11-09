package com.astra.user.application.port.in;

import java.util.Optional;
import java.util.UUID;

public interface GetAuthInfoQuery {

    Optional<AuthInfo> authInfo(UUID userId);

    default boolean canSignIn(UUID userId) {
        return authInfo(userId).filter(info -> !info.banned()).isPresent();
    }
}
