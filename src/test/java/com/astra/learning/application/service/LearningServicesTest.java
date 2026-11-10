package com.astra.learning.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.learning.application.port.in.CourseResponse;
import com.astra.learning.application.port.in.CreateCourseCommand;
import com.astra.learning.application.port.in.ModuleResponse;
import com.astra.learning.application.port.in.UpdateModuleCommand;
import com.astra.learning.application.port.out.CheckCourseOwnershipPort;
import com.astra.learning.application.port.out.CheckGithubRepositoryOwnerPort;
import com.astra.learning.application.port.out.CourseRepositoryLinksPort;
import com.astra.learning.application.port.out.LoadLinkedRepositoriesPort;
import com.astra.learning.application.port.out.CreateCoursePort;
import com.astra.learning.application.port.out.ListCoursesPort;
import com.astra.learning.application.port.out.LoadCoursePort;
import com.astra.learning.application.port.out.LoadLastStudiedPort;
import com.astra.learning.application.port.out.UpdateCoursePort;
import com.astra.learning.domain.BlankModuleTitleException;
import com.astra.learning.domain.Course;
import com.astra.shared.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LearningServicesTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BIA = UUID.randomUUID();

    private final LoadCoursePort loadCoursePort = mock(LoadCoursePort.class);
    private final UpdateCoursePort updateCoursePort = mock(UpdateCoursePort.class);
    private final CreateCoursePort createCoursePort = mock(CreateCoursePort.class);
    private final ListCoursesPort listCoursesPort = mock(ListCoursesPort.class);
    private final LoadLastStudiedPort lastStudiedPort = mock(LoadLastStudiedPort.class);
    private final CheckGithubRepositoryOwnerPort githubOwnerPort = mock(CheckGithubRepositoryOwnerPort.class);
    private final CourseRepositoryLinksPort linksPort = mock(CourseRepositoryLinksPort.class);

    private final CourseQueryService queryService = new CourseQueryService(listCoursesPort, loadCoursePort,
            lastStudiedPort, mock(CheckCourseOwnershipPort.class), linksPort, mock(LoadLinkedRepositoriesPort.class));

    private Course stored(int modules) {
        Course course = Course.plan(ANA, "Spring", null, modules);
        when(loadCoursePort.loadOwned(course.getId(), ANA)).thenReturn(Optional.of(course));
        return course;
    }

    @Test
    void criarGravaOCursoPlanejado() {
        CourseResponse response = new CreateCourseService(createCoursePort)
                .create(new CreateCourseCommand(ANA, "Spring", "Udemy", 2));

        verify(createCoursePort).create(any());
        assertThat(response.status()).isEqualTo("PLANNED");
        assertThat(response.lastStudiedAt()).isNull();
    }

    @Test
    void cursoDeOutraPessoaNaoGravaNada() {
        Course course = stored(1);
        UUID moduleId = course.getModules().getFirst().getId();
        when(loadCoursePort.loadOwned(course.getId(), BIA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CompleteLessonsUpToService(loadCoursePort, updateCoursePort)
                .completeUpTo(BIA, course.getId(), moduleId, 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course not found");
        verify(updateCoursePort, never()).update(any());
    }

    @Test
    void editarModuloSoMudaOQueVeio() {
        Course course = stored(1);
        UUID moduleId = course.getModules().getFirst().getId();
        UpdateModuleService service = new UpdateModuleService(loadCoursePort, updateCoursePort);

        ModuleResponse renamed = service.update(new UpdateModuleCommand(ANA, course.getId(), moduleId, " Novo ", null));
        ModuleResponse completed = service.update(new UpdateModuleCommand(ANA, course.getId(), moduleId, null, true));

        assertThat(renamed.title()).isEqualTo("Novo");
        assertThat(renamed.completed()).isFalse();
        assertThat(completed.title()).isEqualTo("Novo");
        assertThat(completed.completed()).isTrue();
        verify(updateCoursePort, org.mockito.Mockito.times(2)).update(course);
    }

    @Test
    void tituloEmBrancoNaoGrava() {
        Course course = stored(1);
        UUID moduleId = course.getModules().getFirst().getId();

        assertThatThrownBy(() -> new UpdateModuleService(loadCoursePort, updateCoursePort)
                .update(new UpdateModuleCommand(ANA, course.getId(), moduleId, " ", true)))
                .isInstanceOf(BlankModuleTitleException.class);
        verify(updateCoursePort, never()).update(any());
    }

    @Test
    void vincularRepositorioDeOutraPessoaRetorna404() {
        Course course = stored(0);
        UUID repositoryId = UUID.randomUUID();
        LinkGithubRepositoryToCourseService service =
                new LinkGithubRepositoryToCourseService(loadCoursePort, githubOwnerPort, linksPort);

        assertThatThrownBy(() -> service.link(ANA, course.getId(), repositoryId))
                .isInstanceOf(NotFoundException.class);
        verify(linksPort, never()).link(any(), any());

        when(githubOwnerPort.isOwner(repositoryId, ANA)).thenReturn(true);
        service.link(ANA, course.getId(), repositoryId);
        verify(linksPort).link(course.getId(), repositoryId);
    }

    @Test
    void listaJuntaOUltimoEstudoESemCursosNaoConsultaSessoes() {
        Course course = Course.plan(ANA, "Spring", null, 0);
        OffsetDateTime studied = OffsetDateTime.parse("2026-11-01T10:00:00Z");
        when(listCoursesPort.listByUser(ANA)).thenReturn(List.of(course));
        when(lastStudiedPort.byCourse(ANA, List.of(course.getId()))).thenReturn(Map.of(course.getId(), studied));

        assertThat(queryService.list(ANA)).extracting(CourseResponse::lastStudiedAt).containsExactly(studied);
        assertThat(queryService.list(BIA)).isEmpty();
        verify(lastStudiedPort, never()).byCourse(org.mockito.ArgumentMatchers.eq(BIA), anyCollection());
    }
}
