package com.astra.github.application.service;

import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.domain.GitHubConnection;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;

final class Connections {

    private Connections() {
    }

    static GitHubConnection require(LoadConnectionPort loadConnectionPort, UUID userId) {
        return loadConnectionPort.loadByUser(userId).orElseThrow(() -> new NotFoundException("GitHub não conectado"));
    }
}
