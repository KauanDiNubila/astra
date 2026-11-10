package com.astra.github.application.port.in;

import java.util.UUID;

public interface DisconnectGitHubUseCase {

    void disconnect(UUID userId);
}
