package com.astra.user.application.service;

import com.astra.user.application.port.in.DeleteUserUseCase;
import com.astra.user.application.port.out.DeleteUserPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.domain.User;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeleteUserService implements DeleteUserUseCase {

    private final LoadUserPort loadUserPort;
    private final DeleteUserPort deleteUserPort;

    DeleteUserService(LoadUserPort loadUserPort, DeleteUserPort deleteUserPort) {
        this.loadUserPort = loadUserPort;
        this.deleteUserPort = deleteUserPort;
    }

    @Override
    @Transactional
    public void delete(UUID actorId, UUID targetId) {
        User actor = OwnedUser.actor(loadUserPort, actorId);
        User target = actorId.equals(targetId) ? actor : OwnedUser.target(loadUserPort, targetId);
        actor.checkCanDelete(target);
        deleteUserPort.delete(targetId);
    }
}
