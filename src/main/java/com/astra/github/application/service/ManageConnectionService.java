package com.astra.github.application.service;

import com.astra.github.application.port.in.ChangeGitHubVisibilityUseCase;
import com.astra.github.application.port.in.DisconnectGitHubUseCase;
import com.astra.github.application.port.out.DeleteGitHubDataPort;
import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.application.port.out.UpdateConnectionPort;
import com.astra.github.domain.GitHubConnection;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ManageConnectionService implements ChangeGitHubVisibilityUseCase, DisconnectGitHubUseCase {

    private final LoadConnectionPort loadConnectionPort;
    private final UpdateConnectionPort updateConnectionPort;
    private final DeleteGitHubDataPort deleteGitHubDataPort;

    ManageConnectionService(LoadConnectionPort loadConnectionPort, UpdateConnectionPort updateConnectionPort,
                            DeleteGitHubDataPort deleteGitHubDataPort) {
        this.loadConnectionPort = loadConnectionPort;
        this.updateConnectionPort = updateConnectionPort;
        this.deleteGitHubDataPort = deleteGitHubDataPort;
    }

    @Override
    @Transactional
    public void change(UUID userId, boolean visibleToFriends) {
        GitHubConnection connection = Connections.require(loadConnectionPort, userId);
        connection.changeVisibility(visibleToFriends);
        updateConnectionPort.update(connection);
    }

    @Override
    @Transactional
    public void disconnect(UUID userId) {
        deleteGitHubDataPort.deleteAllFor(userId);
    }
}
