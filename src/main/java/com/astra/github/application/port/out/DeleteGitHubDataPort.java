package com.astra.github.application.port.out;

import java.util.UUID;

public interface DeleteGitHubDataPort {

    void deleteAllFor(UUID userId);
}
