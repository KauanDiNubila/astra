package com.astra.user.application.service;

import com.astra.user.application.port.in.OAuthLogInCommand;
import com.astra.user.application.port.in.OAuthLogInUseCase;
import com.astra.user.application.port.out.CreateUserPort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.OAuthConnectionPort;
import com.astra.user.application.port.out.PublishUserRegisteredPort;
import com.astra.user.domain.OAuthConnection;
import com.astra.user.domain.User;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OAuthLogInService implements OAuthLogInUseCase {

    private final OAuthConnectionPort oAuthConnectionPort;
    private final LoadUserPort loadUserPort;
    private final CreateUserPort createUserPort;
    private final PublishUserRegisteredPort publishUserRegisteredPort;
    private final TagGenerator tagGenerator;
    private final Clock clock;

    OAuthLogInService(OAuthConnectionPort oAuthConnectionPort, LoadUserPort loadUserPort, CreateUserPort createUserPort, PublishUserRegisteredPort publishUserRegisteredPort, TagGenerator tagGenerator, Clock clock) {
        this.oAuthConnectionPort = oAuthConnectionPort;
        this.loadUserPort = loadUserPort;
        this.createUserPort = createUserPort;
        this.publishUserRegisteredPort = publishUserRegisteredPort;
        this.tagGenerator = tagGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UUID logIn(OAuthLogInCommand command) {
        Optional<UUID> connected = oAuthConnectionPort.findUserId(command.provider(), command.providerUserId());
        if (connected.isPresent()) {
            return connected.get();
        }
        User user = loadUserPort.loadByEmail(command.email()).orElseGet(() -> {
            User created = createUserPort.create(User.fromOAuth(command.name(), command.email(),
                    tagGenerator.generate(command.name()), OffsetDateTime.now(clock)));
            publishUserRegisteredPort.publish(created.getId());
            return created;
        });
        oAuthConnectionPort.create(OAuthConnection.link(user.getId(), command.provider(), command.providerUserId()));
        return user.getId();
    }
}
