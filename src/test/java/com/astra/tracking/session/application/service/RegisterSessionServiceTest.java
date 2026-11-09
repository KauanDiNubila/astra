package com.astra.tracking.session.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.session.application.port.in.RegisterSessionCommand;
import com.astra.tracking.session.application.port.in.SessionResponse;
import com.astra.tracking.session.application.port.out.CheckCategoryOwnerPort;
import com.astra.tracking.session.application.port.out.CheckCourseOwnerPort;
import com.astra.tracking.session.application.port.out.CreateSessionPort;
import com.astra.tracking.session.domain.Session;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterSessionServiceTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID CATEGORY = UUID.randomUUID();
    private static final UUID COURSE = UUID.randomUUID();
    private static final OffsetDateTime STARTED_AT = OffsetDateTime.parse("2026-08-03T14:00:00-03:00");

    private final CheckCategoryOwnerPort checkCategoryOwnerPort = mock(CheckCategoryOwnerPort.class);
    private final CheckCourseOwnerPort checkCourseOwnerPort = mock(CheckCourseOwnerPort.class);
    private final CreateSessionPort createSessionPort = mock(CreateSessionPort.class);
    private final RegisterSessionService service =
            new RegisterSessionService(checkCategoryOwnerPort, checkCourseOwnerPort, createSessionPort);

    private RegisterSessionCommand command(UUID courseId) {
        return new RegisterSessionCommand(USER, CATEGORY, courseId, 50, STARTED_AT, "estudo");
    }

    private void createReturnsSavedCopy() {
        when(createSessionPort.create(any())).thenAnswer(invocation -> {
            Session s = invocation.getArgument(0);
            return Session.withId(UUID.randomUUID(), s.getUserId(), s.getCategoryId(), s.getCourseId(),
                    s.getGithubRepositoryId(), s.getFocusedMinutes(), s.getStartedAt(), s.getNote(), STARTED_AT);
        });
    }

    @Test
    void registraSessaoNaCategoriaDoUsuario() {
        when(checkCategoryOwnerPort.isOwner(CATEGORY, USER)).thenReturn(true);
        createReturnsSavedCopy();

        SessionResponse response = service.register(command(null));

        assertThat(response.id()).isNotNull();
        assertThat(response.categoryId()).isEqualTo(CATEGORY);
        assertThat(response.focusedMinutes()).isEqualTo(50);
        assertThat(response.note()).isEqualTo("estudo");
        verifyNoInteractions(checkCourseOwnerPort);
    }

    @Test
    void recusaCategoriaQueNaoEDoUsuario() {
        when(checkCategoryOwnerPort.isOwner(CATEGORY, USER)).thenReturn(false);

        assertThatThrownBy(() -> service.register(command(null))).isInstanceOf(NotFoundException.class);
        verify(createSessionPort, never()).create(any());
    }

    @Test
    void registraComCursoDoUsuario() {
        when(checkCategoryOwnerPort.isOwner(CATEGORY, USER)).thenReturn(true);
        when(checkCourseOwnerPort.isOwner(COURSE, USER)).thenReturn(true);
        createReturnsSavedCopy();

        assertThat(service.register(command(COURSE)).courseId()).isEqualTo(COURSE);
    }

    @Test
    void recusaCursoQueNaoEDoUsuario() {
        when(checkCategoryOwnerPort.isOwner(CATEGORY, USER)).thenReturn(true);
        when(checkCourseOwnerPort.isOwner(COURSE, USER)).thenReturn(false);

        assertThatThrownBy(() -> service.register(command(COURSE))).isInstanceOf(NotFoundException.class);
        verify(createSessionPort, never()).create(any());
    }
}
