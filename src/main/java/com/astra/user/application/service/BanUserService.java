package com.astra.user.application.service;

import com.astra.user.application.port.in.BanUserUseCase;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BanUserService implements BanUserUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;
    private final Clock clock;

    BanUserService(LoadUserPort loadUserPort, UpdateUserPort updateUserPort, Clock clock) {
        this.loadUserPort = loadUserPort;
        this.updateUserPort = updateUserPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void ban(UUID actorId, UUID targetId) {
        User actor = OwnedUser.actor(loadUserPort, actorId);
        User target = actorId.equals(targetId) ? actor : OwnedUser.target(loadUserPort, targetId);
        actor.ban(target, OffsetDateTime.now(clock));
        updateUserPort.update(target);
    }

    @Override
    @Transactional
    public void unban(UUID actorId, UUID targetId) {
        User actor = OwnedUser.actor(loadUserPort, actorId);
        User target = OwnedUser.target(loadUserPort, targetId);
        actor.unban(target);
        updateUserPort.update(target);
    }
}
