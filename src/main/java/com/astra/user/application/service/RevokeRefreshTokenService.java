package com.astra.user.application.service;

import com.astra.user.application.port.in.RevokeRefreshTokenUseCase;
import com.astra.user.application.port.out.LoadRefreshTokenPort;
import com.astra.user.application.port.out.UpdateRefreshTokenPort;
import com.astra.user.domain.RefreshToken;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RevokeRefreshTokenService implements RevokeRefreshTokenUseCase {

    private final LoadRefreshTokenPort loadRefreshTokenPort;
    private final UpdateRefreshTokenPort updateRefreshTokenPort;
    private final Clock clock;

    RevokeRefreshTokenService(LoadRefreshTokenPort loadRefreshTokenPort, UpdateRefreshTokenPort updateRefreshTokenPort,
                              Clock clock) {
        this.loadRefreshTokenPort = loadRefreshTokenPort;
        this.updateRefreshTokenPort = updateRefreshTokenPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        loadRefreshTokenPort.loadByHash(RefreshToken.hash(rawToken)).ifPresent(token -> {
            token.revoke(OffsetDateTime.now(clock));
            updateRefreshTokenPort.update(token);
        });
    }
}
