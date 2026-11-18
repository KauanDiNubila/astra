package com.astra.user.application.port.out;

import com.astra.user.domain.PasswordResetToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenPort {

    void create(PasswordResetToken token);

    Optional<PasswordResetToken> loadByHash(String tokenHash);

    Optional<PasswordResetToken> loadLatestFor(UUID userId);

    int invalidateAllFor(UUID userId, OffsetDateTime now);
}
