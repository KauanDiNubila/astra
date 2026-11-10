package com.astra.user.application.service;

import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.in.RedeemDesktopLoginCodeUseCase;
import com.astra.user.application.port.out.DesktopLoginCodeStorePort;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class RedeemDesktopLoginCodeService implements RedeemDesktopLoginCodeUseCase {

    private final DesktopLoginCodeStorePort storePort;
    private final Clock clock;

    RedeemDesktopLoginCodeService(DesktopLoginCodeStorePort storePort, Clock clock) {
        this.storePort = storePort;
        this.clock = clock;
    }

    @Override
    public UUID redeem(String code, String verifier) {
        return Optional.ofNullable(code)
                .flatMap(storePort::take)
                .filter(loginCode -> !loginCode.isExpired(clock.instant()))
                .filter(loginCode -> loginCode.acceptsVerifier(verifier))
                .map(loginCode -> loginCode.userId())
                .orElseThrow(() -> new UnauthorizedException("Código de login inválido ou expirado"));
    }
}
