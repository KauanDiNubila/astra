package com.astra.user.application.service;

import com.astra.user.application.port.in.ChangeUserRoleUseCase;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.User;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ChangeUserRoleService implements ChangeUserRoleUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;

    ChangeUserRoleService(LoadUserPort loadUserPort, UpdateUserPort updateUserPort) {
        this.loadUserPort = loadUserPort;
        this.updateUserPort = updateUserPort;
    }

    @Override
    @Transactional
    public void promote(UUID actorId, UUID targetId) {
        User actor = OwnedUser.actor(loadUserPort, actorId);
        User target = OwnedUser.target(loadUserPort, targetId);
        actor.promote(target);
        updateUserPort.update(target);
    }

    @Override
    @Transactional
    public void demote(UUID actorId, UUID targetId) {
        User actor = OwnedUser.actor(loadUserPort, actorId);
        User target = OwnedUser.target(loadUserPort, targetId);
        actor.demote(target);
        updateUserPort.update(target);
    }
}
