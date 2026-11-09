package com.astra.tracking.session.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.session.application.port.in.LinkRepositoryToSessionUseCase;
import com.astra.tracking.session.application.port.in.SessionResponse;
import com.astra.tracking.session.application.port.out.CheckGithubRepositoryOwnerPort;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.application.port.out.UpdateSessionRepositoryPort;
import com.astra.tracking.session.domain.Session;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

class LinkRepositoryToSessionService implements LinkRepositoryToSessionUseCase {

    private final LoadSessionPort loadSessionPort;
    private final CheckGithubRepositoryOwnerPort checkGithubRepositoryOwnerPort;
    private final UpdateSessionRepositoryPort updateSessionRepositoryPort;

    LinkRepositoryToSessionService(LoadSessionPort loadSessionPort,
                                   CheckGithubRepositoryOwnerPort checkGithubRepositoryOwnerPort,
                                   UpdateSessionRepositoryPort updateSessionRepositoryPort) {
        this.loadSessionPort = loadSessionPort;
        this.checkGithubRepositoryOwnerPort = checkGithubRepositoryOwnerPort;
        this.updateSessionRepositoryPort = updateSessionRepositoryPort;
    }

    @Override
    @Transactional
    public SessionResponse link(UUID userId, UUID sessionId, UUID repositoryId) {
        Session session = OwnedSession.load(loadSessionPort, userId, sessionId);
        if (!checkGithubRepositoryOwnerPort.isOwner(repositoryId, userId)) {
            throw new NotFoundException("Repositório não encontrado");
        }
        session.linkRepository(repositoryId);
        updateSessionRepositoryPort.updateRepository(session);
        return SessionResponse.from(session);
    }
}
