package com.astra.user.application.service;

import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.in.LogInCommand;
import com.astra.user.application.port.in.LogInUseCase;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class LogInService implements LogInUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordHasherPort passwordHasherPort;

    LogInService(LoadUserPort loadUserPort, PasswordHasherPort passwordHasherPort) {
        this.loadUserPort = loadUserPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    public UserResponse logIn(LogInCommand command) {
        User user = loadUserPort.loadByEmail(command.email())
                .filter(u -> u.hasPassword() && passwordHasherPort.matches(command.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        return UserResponse.from(user);
    }
}
