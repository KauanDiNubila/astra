package com.astra.user.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RevokeAllRefreshTokensPort {

    void revokeAllFor(UUID userId, OffsetDateTime now);
}
