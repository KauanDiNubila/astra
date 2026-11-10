package com.astra.github.application.port.in;

import java.util.UUID;

public interface GetGitHubActivityQuery {

    GitHubActivityResponse activity(UUID userId);
}
