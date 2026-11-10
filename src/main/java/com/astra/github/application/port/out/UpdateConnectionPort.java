package com.astra.github.application.port.out;

import com.astra.github.domain.GitHubConnection;

public interface UpdateConnectionPort {

    void update(GitHubConnection connection);
}
