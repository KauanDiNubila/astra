package com.astra.user.application.service;

import com.astra.user.application.port.in.UpdateAvatarCommand;
import com.astra.user.application.port.in.UpdateAvatarUseCase;
import com.astra.user.application.port.out.AvatarPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.domain.Avatar;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateAvatarService implements UpdateAvatarUseCase {

    private final LoadUserPort loadUserPort;
    private final AvatarPort avatarPort;

    UpdateAvatarService(LoadUserPort loadUserPort, AvatarPort avatarPort) {
        this.loadUserPort = loadUserPort;
        this.avatarPort = avatarPort;
    }

    @Override
    @Transactional
    public void update(UpdateAvatarCommand command) {
        OwnedUser.actor(loadUserPort, command.userId());
        avatarPort.save(command.userId(), Avatar.of(command.contentType(), command.bytes()));
    }
}
