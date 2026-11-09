package com.astra.user.application.service;

import com.astra.shared.exception.ConflictException;
import com.astra.user.application.port.in.RegisterUserCommand;
import com.astra.user.application.port.in.RegisterUserUseCase;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.out.CheckPasswordBreachPort;
import com.astra.user.application.port.out.CreateUserPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.PasswordHasherPort;
import com.astra.user.application.port.out.PublishUserRegisteredPort;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RegisterUserService implements RegisterUserUseCase {

    private final CheckPasswordBreachPort checkPasswordBreachPort;
    private final LoadUserPort loadUserPort;
    private final CreateUserPort createUserPort;
    private final PasswordHasherPort passwordHasherPort;
    private final PublishUserRegisteredPort publishUserRegisteredPort;
    private final TagGenerator tagGenerator;
    private final Clock clock;

    RegisterUserService(CheckPasswordBreachPort checkPasswordBreachPort, LoadUserPort loadUserPort, CreateUserPort createUserPort, PasswordHasherPort passwordHasherPort, PublishUserRegisteredPort publishUserRegisteredPort, TagGenerator tagGenerator, Clock clock) {
        this.checkPasswordBreachPort = checkPasswordBreachPort;
        this.loadUserPort = loadUserPort;
        this.createUserPort = createUserPort;
        this.passwordHasherPort = passwordHasherPort;
        this.publishUserRegisteredPort = publishUserRegisteredPort;
        this.tagGenerator = tagGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserCommand command) {
        if (checkPasswordBreachPort.isBreached(command.password())) {
            throw new ConflictException("Essa senha já apareceu em vazamentos conhecidos - escolha outra");
        }
        if (loadUserPort.loadByEmail(command.email()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        User user = User.register(command.name(), command.email(), passwordHasherPort.hash(command.password()),
                tagGenerator.generate(command.name()));
        if (command.acceptTerms()) {
            user.acceptTerms(OffsetDateTime.now(clock));
        }
        User created = createUserPort.create(user);
        publishUserRegisteredPort.publish(created.getId());
        return UserResponse.from(created);
    }
}
