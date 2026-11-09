package com.astra.user.application.port.out;

import java.util.UUID;

public interface PublishUserRegisteredPort {

    void publish(UUID userId);
}
