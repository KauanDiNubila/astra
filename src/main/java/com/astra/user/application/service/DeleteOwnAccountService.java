package com.astra.user.application.service;

import com.astra.shared.exception.ConflictException;
import com.astra.user.application.port.in.DeleteOwnAccountCommand;
import com.astra.user.application.port.in.DeleteOwnAccountUseCase;
import com.astra.user.application.port.out.DeleteUserPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeleteOwnAccountService implements DeleteOwnAccountUseCase {

    private final LoadUserPort loadUserPort;
    private final DeleteUserPort deleteUserPort;
    private final PasswordHasherPort passwordHasherPort;

    DeleteOwnAccountService(LoadUserPort loadUserPort, DeleteUserPort deleteUserPort, PasswordHasherPort passwordHasherPort) {
        this.loadUserPort = loadUserPort;
        this.deleteUserPort = deleteUserPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    @Transactional
    public void delete(DeleteOwnAccountCommand command) {
        User user = OwnedUser.actor(loadUserPort, command.userId());
        user.checkCanDeleteOwnAccount();
        if (user.hasPassword()) {
            if (command.password() == null
                    || !passwordHasherPort.matches(command.password(), user.getPasswordHash())) {
                throw new ConflictException("Senha incorreta");
            }
        } else if (!user.isConfirmedByEmail(command.confirmation())) {
            throw new ConflictException("Digite o e-mail da sua conta para confirmar");
        }
        deleteUserPort.delete(user.getId());
    }
}
