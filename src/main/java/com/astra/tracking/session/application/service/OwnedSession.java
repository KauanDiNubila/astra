package com.astra.tracking.session.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.domain.Session;
import java.util.UUID;

final class OwnedSession {

    private OwnedSession() {
    }

    static Session load(LoadSessionPort loadSessionPort, UUID userId, UUID sessionId) {
        return loadSessionPort.load(sessionId)
                .filter(session -> session.belongsTo(userId))
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }
}
