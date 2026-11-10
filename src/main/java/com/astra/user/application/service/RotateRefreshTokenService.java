package com.astra.user.application.service;

import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.in.RotateRefreshTokenUseCase;
import com.astra.user.application.port.out.LoadRefreshTokenPort;
import com.astra.user.application.port.out.RevokeAllRefreshTokensPort;
import com.astra.user.application.port.out.UpdateRefreshTokenPort;
import com.astra.user.domain.RefreshToken;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RotateRefreshTokenService implements RotateRefreshTokenUseCase {

    private final LoadRefreshTokenPort loadRefreshTokenPort;
    private final UpdateRefreshTokenPort updateRefreshTokenPort;
    private final RevokeAllRefreshTokensPort revokeAllRefreshTokensPort;
    private final Clock clock;

    RotateRefreshTokenService(LoadRefreshTokenPort loadRefreshTokenPort, UpdateRefreshTokenPort updateRefreshTokenPort,
                              RevokeAllRefreshTokensPort revokeAllRefreshTokensPort, Clock clock) {
        this.loadRefreshTokenPort = loadRefreshTokenPort;
        this.updateRefreshTokenPort = updateRefreshTokenPort;
        this.revokeAllRefreshTokensPort = revokeAllRefreshTokensPort;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public UUID rotate(String rawToken) {
        RefreshToken token = loadRefreshTokenPort.loadByHash(RefreshToken.hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        OffsetDateTime now = OffsetDateTime.now(clock);
        switch (token.rotate(now)) {
            case ROTATED -> updateRefreshTokenPort.update(token);
            case CONCURRENT -> {
            }
            case REUSED -> {
                revokeAllRefreshTokensPort.revokeAllFor(token.getUserId(), now);
                throw new UnauthorizedException("Refresh token inválido");
            }
            case EXPIRED -> throw new UnauthorizedException("Refresh token expirado");
        }
        return token.getUserId();
    }
}
