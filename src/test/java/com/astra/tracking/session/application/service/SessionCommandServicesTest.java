package com.astra.tracking.session.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.session.application.port.out.CheckGithubRepositoryOwnerPort;
import com.astra.tracking.session.application.port.out.DeleteSessionPort;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.application.port.out.UpdateSessionRepositoryPort;
import com.astra.tracking.session.domain.Session;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionCommandServicesTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID OTHER_USER = UUID.randomUUID();
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID REPOSITORY = UUID.randomUUID();

    private final LoadSessionPort loadSessionPort = mock(LoadSessionPort.class);
    private final DeleteSessionPort deleteSessionPort = mock(DeleteSessionPort.class);
    private final UpdateSessionRepositoryPort updateSessionRepositoryPort = mock(UpdateSessionRepositoryPort.class);
    private final CheckGithubRepositoryOwnerPort checkGithubRepositoryOwnerPort =
            mock(CheckGithubRepositoryOwnerPort.class);

    private final DeleteSessionService deleteService = new DeleteSessionService(loadSessionPort, deleteSessionPort);
    private final LinkRepositoryToSessionService linkService = new LinkRepositoryToSessionService(
            loadSessionPort, checkGithubRepositoryOwnerPort, updateSessionRepositoryPort);
    private final UnlinkRepositoryFromSessionService unlinkService =
            new UnlinkRepositoryFromSessionService(loadSessionPort, updateSessionRepositoryPort);

    private Session storedSession(UUID owner, UUID repositoryId) {
        Session session = Session.withId(SESSION_ID, owner, UUID.randomUUID(), null, repositoryId, 30,
                OffsetDateTime.parse("2026-08-03T14:00:00-03:00"), null, OffsetDateTime.now());
        when(loadSessionPort.load(SESSION_ID)).thenReturn(Optional.of(session));
        return session;
    }

    @Test
    void excluiSessaoDoUsuario() {
        storedSession(USER, null);

        deleteService.delete(USER, SESSION_ID);

        verify(deleteSessionPort).delete(SESSION_ID);
    }

    @Test
    void naoExcluiSessaoDeOutroUsuario() {
        storedSession(OTHER_USER, null);

        assertThatThrownBy(() -> deleteService.delete(USER, SESSION_ID)).isInstanceOf(NotFoundException.class);
        verify(deleteSessionPort, never()).delete(any());
    }

    @Test
    void naoExcluiSessaoInexistente() {
        when(loadSessionPort.load(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteService.delete(USER, SESSION_ID)).isInstanceOf(NotFoundException.class);
        verify(deleteSessionPort, never()).delete(any());
    }

    @Test
    void vinculaRepositorioDoUsuario() {
        Session session = storedSession(USER, null);
        when(checkGithubRepositoryOwnerPort.isOwner(REPOSITORY, USER)).thenReturn(true);

        assertThat(linkService.link(USER, SESSION_ID, REPOSITORY).githubRepositoryId()).isEqualTo(REPOSITORY);
        verify(updateSessionRepositoryPort).updateRepository(session);
    }

    @Test
    void naoVinculaRepositorioDeOutroUsuario() {
        storedSession(USER, null);
        when(checkGithubRepositoryOwnerPort.isOwner(REPOSITORY, USER)).thenReturn(false);

        assertThatThrownBy(() -> linkService.link(USER, SESSION_ID, REPOSITORY))
                .isInstanceOf(NotFoundException.class);
        verify(updateSessionRepositoryPort, never()).updateRepository(any());
    }

    @Test
    void naoVinculaEmSessaoDeOutroUsuario() {
        storedSession(OTHER_USER, null);

        assertThatThrownBy(() -> linkService.link(USER, SESSION_ID, REPOSITORY))
                .isInstanceOf(NotFoundException.class);
        verify(updateSessionRepositoryPort, never()).updateRepository(any());
    }

    @Test
    void desvinculaRepositorio() {
        Session session = storedSession(USER, REPOSITORY);

        assertThat(unlinkService.unlink(USER, SESSION_ID).githubRepositoryId()).isNull();
        verify(updateSessionRepositoryPort).updateRepository(session);
    }

    @Test
    void naoDesvinculaEmSessaoDeOutroUsuario() {
        storedSession(OTHER_USER, REPOSITORY);

        assertThatThrownBy(() -> unlinkService.unlink(USER, SESSION_ID)).isInstanceOf(NotFoundException.class);
        verify(updateSessionRepositoryPort, never()).updateRepository(any());
    }
}
