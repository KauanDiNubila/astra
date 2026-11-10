package com.astra.github.application.port.in;

import java.util.UUID;

public interface SyncGitHubUseCase {

    void sync(UUID userId);
}
