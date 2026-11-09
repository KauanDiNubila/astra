package com.astra.tracking.session.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.session.application.port.in.GetSessionWindowQuery.SessionWindow;
import com.astra.tracking.session.application.port.out.ListSessionsPort;
import com.astra.tracking.session.application.port.out.LoadSessionPort;
import com.astra.tracking.session.application.port.out.LoadSessionStatsPort;
import com.astra.tracking.session.domain.Session;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionQueryServiceTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final OffsetDateTime STARTED_AT = OffsetDateTime.parse("2026-08-03T14:00:00-03:00");

    private final ListSessionsPort listSessionsPort = mock(ListSessionsPort.class);
    private final LoadSessionPort loadSessionPort = mock(LoadSessionPort.class);
    private final LoadSessionStatsPort loadSessionStatsPort = mock(LoadSessionStatsPort.class);
    private final SessionQueryService service =
            new SessionQueryService(listSessionsPort, loadSessionPort, loadSessionStatsPort);

    private Session session(UUID owner, UUID repositoryId) {
        return Session.withId(SESSION_ID, owner, UUID.randomUUID(), null, repositoryId, 90, STARTED_AT, null,
                STARTED_AT);
    }

    @Test
    void listaComPaginaETamanhoAjustados() {
        when(listSessionsPort.listNewestFirst(USER, 0, 1)).thenReturn(List.of(session(USER, null)));

        assertThat(service.list(USER, -3, 0)).hasSize(1);
        service.list(USER, 2, 5000);

        verify(listSessionsPort).listNewestFirst(USER, 2, SessionQueryService.MAX_PAGE_SIZE);
    }

    @Test
    void janelaDaSessaoVaiDoInicioAoFim() {
        UUID repository = UUID.randomUUID();
        when(loadSessionPort.load(SESSION_ID)).thenReturn(Optional.of(session(USER, repository)));

        assertThat(service.get(USER, SESSION_ID)).isEqualTo(
                new SessionWindow(STARTED_AT, OffsetDateTime.parse("2026-08-03T15:30:00-03:00"), repository));
    }

    @Test
    void janelaDeSessaoDeOutroUsuarioRetornaNaoEncontrada() {
        when(loadSessionPort.load(SESSION_ID)).thenReturn(Optional.of(session(UUID.randomUUID(), null)));

        assertThatThrownBy(() -> service.get(USER, SESSION_ID)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void colecoesVaziasNaoConsultamOBanco() {
        assertThat(service.sinceForUsers(STARTED_AT, Set.of())).isEmpty();
        assertThat(service.byCourse(USER, Set.of())).isEmpty();

        verifyNoInteractions(loadSessionStatsPort);
    }
}
