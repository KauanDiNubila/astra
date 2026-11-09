package com.astra.tracking.session.application.service;

import com.astra.tracking.session.application.port.in.SessionResponse;
import com.astra.tracking.session.application.port.in.UnlinkRepositoryFromSessionUseCase;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.application.port.out.UpdateSessionRepositoryPort;
import com.astra.tracking.session.domain.Session;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UnlinkRepositoryFromSessionService implements UnlinkRepositoryFromSessionUseCase {

    private final LoadSessionPort loadSessionPort;
    private final UpdateSessionRepositoryPort updateSessionRepositoryPort;

    UnlinkRepositoryFromSessionService(LoadSessionPort loadSessionPort,
                                       UpdateSessionRepositoryPort updateSessionRepositoryPort) {
        this.loadSessionPort = loadSessionPort;
        this.updateSessionRepositoryPort = updateSessionRepositoryPort;
    }

    @Override
    @Transactional
    public SessionResponse unlink(UUID userId, UUID sessionId) {
        Session session = OwnedSession.load(loadSessionPort, userId, sessionId);
        session.unlinkRepository();
        updateSessionRepositoryPort.updateRepository(session);
        return SessionResponse.from(session);
    }
}
