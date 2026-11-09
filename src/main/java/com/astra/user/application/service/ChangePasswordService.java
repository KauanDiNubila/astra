package com.astra.user.application.service;

import com.astra.shared.exception.ConflictException;
import com.astra.user.application.port.in.ChangePasswordCommand;
import com.astra.user.application.port.in.ChangePasswordUseCase;
import com.astra.user.application.port.out.CheckPasswordBreachPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.application.port.out.UpdateUserPort;
import com.astra.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ChangePasswordService implements ChangePasswordUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateUserPort updateUserPort;
    private final PasswordHasherPort passwordHasherPort;
    private final CheckPasswordBreachPort checkPasswordBreachPort;

    ChangePasswordService(LoadUserPort loadUserPort, UpdateUserPort updateUserPort, PasswordHasherPort passwordHasherPort, CheckPasswordBreachPort checkPasswordBreachPort) {
        this.loadUserPort = loadUserPort;
        this.updateUserPort = updateUserPort;
        this.passwordHasherPort = passwordHasherPort;
        this.checkPasswordBreachPort = checkPasswordBreachPort;
    }

    @Override
    @Transactional
    public void change(ChangePasswordCommand command) {
        User user = OwnedUser.actor(loadUserPort, command.userId());
        user.requirePassword();
        if (!passwordHasherPort.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new ConflictException("Senha atual incorreta");
        }
        if (checkPasswordBreachPort.isBreached(command.newPassword())) {
            throw new ConflictException("Essa senha já apareceu em vazamentos conhecidos - escolha outra");
        }
        user.changePasswordHash(passwordHasherPort.hash(command.newPassword()));
        updateUserPort.update(user);
    }
}
