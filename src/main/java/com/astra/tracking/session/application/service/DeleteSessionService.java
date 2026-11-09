package com.astra.tracking.session.application.service;

import com.astra.tracking.session.application.port.in.DeleteSessionUseCase;
import com.astra.tracking.session.application.port.out.DeleteSessionPort;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeleteSessionService implements DeleteSessionUseCase {

    private final LoadSessionPort loadSessionPort;
    private final DeleteSessionPort deleteSessionPort;

    DeleteSessionService(LoadSessionPort loadSessionPort, DeleteSessionPort deleteSessionPort) {
        this.loadSessionPort = loadSessionPort;
        this.deleteSessionPort = deleteSessionPort;
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID sessionId) {
        OwnedSession.load(loadSessionPort, userId, sessionId);
        deleteSessionPort.delete(sessionId);
    }
}
