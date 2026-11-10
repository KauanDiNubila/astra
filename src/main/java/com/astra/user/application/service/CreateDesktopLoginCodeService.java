package com.astra.user.application.service;

import com.astra.user.application.port.in.CreateDesktopLoginCodeUseCase;
import com.astra.user.application.port.out.DesktopLoginCodeStorePort;
import com.astra.user.domain.DesktopLoginCode;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class CreateDesktopLoginCodeService implements CreateDesktopLoginCodeUseCase {

    private final DesktopLoginCodeStorePort storePort;
    private final Clock clock;
    private final Duration codeTtl;

    CreateDesktopLoginCodeService(DesktopLoginCodeStorePort storePort, Clock clock,
                                  @Value("${astra.desktop-login.code-ttl-seconds:120}") long codeTtlSeconds) {
        this.storePort = storePort;
        this.clock = clock;
        this.codeTtl = Duration.ofSeconds(codeTtlSeconds);
    }

    @Override
    public String create(UUID userId, String challenge) {
        String code = RandomTokens.next();
        storePort.save(code, new DesktopLoginCode(userId, challenge, clock.instant().plus(codeTtl)));
        return code;
    }
}
