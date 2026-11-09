package com.astra.user.application.service;

import com.astra.user.application.port.in.UpdateProfileCommand;
import com.astra.user.application.port.in.UpdateProfileUseCase;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateProfileService implements UpdateProfileUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;
    private final TagGenerator tagGenerator;

    UpdateProfileService(LoadUserPort loadUserPort, UpdateUserPort updateUserPort, TagGenerator tagGenerator) {
        this.loadUserPort = loadUserPort;
        this.updateUserPort = updateUserPort;
        this.tagGenerator = tagGenerator;
    }

    @Override
    @Transactional
    public UserResponse update(UpdateProfileCommand command) {
        User user = OwnedUser.actor(loadUserPort, command.userId());
        String tag = command.name().equals(user.getName())
                ? user.getTag()
                : tagGenerator.generateOrKeep(command.name(), user.getTag());
        user.changeProfile(command.name(), tag, command.bio());
        updateUserPort.update(user);
        return UserResponse.from(user);
    }
}
