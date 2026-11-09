package com.astra.user.application.service;

import com.astra.user.application.port.in.AcceptTermsUseCase;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AcceptTermsService implements AcceptTermsUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;
    private final Clock clock;

    AcceptTermsService(LoadUserPort loadUserPort, UpdateUserPort updateUserPort, Clock clock) {
        this.loadUserPort = loadUserPort;
        this.updateUserPort = updateUserPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserResponse accept(UUID userId) {
        User user = OwnedUser.actor(loadUserPort, userId);
        user.acceptTerms(OffsetDateTime.now(clock));
        updateUserPort.update(user);
        return UserResponse.from(user);
    }
}
