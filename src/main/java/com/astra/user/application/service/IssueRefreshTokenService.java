package com.astra.user.application.service;

import com.astra.user.application.port.in.IssueRefreshTokenUseCase;
import com.astra.user.application.port.out.CreateRefreshTokenPort;
import com.astra.user.domain.RefreshToken;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class IssueRefreshTokenService implements IssueRefreshTokenUseCase {

    private final CreateRefreshTokenPort createRefreshTokenPort;
    private final Clock clock;
    private final Duration lifetime;

    IssueRefreshTokenService(CreateRefreshTokenPort createRefreshTokenPort, Clock clock,
                             @Value("${astra.jwt.refresh-expiration-days}") long refreshExpirationDays) {
        this.createRefreshTokenPort = createRefreshTokenPort;
        this.clock = clock;
        this.lifetime = Duration.ofDays(refreshExpirationDays);
    }

    @Override
    @Transactional
    public String issue(UUID userId) {
        String rawToken = RandomTokens.next();
        createRefreshTokenPort.create(RefreshToken.issue(userId, rawToken, OffsetDateTime.now(clock), lifetime));
        return rawToken;
    }
}
