package com.astra.github.application.port.out;

import com.astra.github.domain.GitHubConnection;

public interface CreateConnectionPort {

    void create(GitHubConnection connection);
}
