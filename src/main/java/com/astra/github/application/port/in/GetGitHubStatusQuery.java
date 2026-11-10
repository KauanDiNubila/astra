package com.astra.github.application.port.in;

import java.util.UUID;

public interface GetGitHubStatusQuery {

    GitHubConnectionStatus status(UUID userId);
}
