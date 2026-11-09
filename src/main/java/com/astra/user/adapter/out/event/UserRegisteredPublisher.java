package com.astra.user.adapter.out.event;

import com.astra.shared.event.UserRegisteredEvent;
import com.astra.user.application.port.out.PublishUserRegisteredPort;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class UserRegisteredPublisher implements PublishUserRegisteredPort {

    private final ApplicationEventPublisher eventPublisher;

    UserRegisteredPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(UUID userId) {
        eventPublisher.publishEvent(new UserRegisteredEvent(userId));
    }
}
