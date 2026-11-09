package com.astra.roadmap.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.roadmap.application.port.in.PinCourseCommand;
import com.astra.roadmap.application.port.in.PinResponse;
import com.astra.roadmap.application.port.in.RoadmapResponse;
import com.astra.roadmap.application.port.in.StepResponse;
import com.astra.roadmap.application.port.out.DeleteCoursePinPort;
import com.astra.roadmap.application.port.out.ListRoadmapsPort;
import com.astra.roadmap.application.port.out.LoadCoursePinPort;
import com.astra.roadmap.application.port.out.LoadRoadmapPort;
import com.astra.roadmap.application.port.out.LoadStepPort;
import com.astra.roadmap.application.port.out.LoadStepStatusesPort;
import com.astra.roadmap.application.port.out.SaveCoursePinPort;
import com.astra.roadmap.application.port.out.SaveStepStatusPort;
import com.astra.roadmap.application.port.out.UserCoursesPort;
import com.astra.roadmap.domain.CoursePin;
import com.astra.roadmap.domain.LocatedStep;
import com.astra.roadmap.domain.RoadmapOutline;
import com.astra.roadmap.domain.RoadmapStep;
import com.astra.roadmap.domain.StepNode;
import com.astra.roadmap.domain.StepStatus;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoadmapServicesTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BIA = UUID.randomUUID();
    private static final UUID ROADMAP = UUID.randomUUID();
    private static final UUID COURSE = UUID.randomUUID();

    private final LoadStepPort loadStepPort = mock(LoadStepPort.class);
    private final SaveStepStatusPort saveStepStatusPort = mock(SaveStepStatusPort.class);
    private final LoadStepStatusesPort statusesPort = mock(LoadStepStatusesPort.class);
    private final ListRoadmapsPort listRoadmapsPort = mock(ListRoadmapsPort.class);
    private final UserCoursesPort userCoursesPort = mock(UserCoursesPort.class);
    private final LoadCoursePinPort loadPinPort = mock(LoadCoursePinPort.class);
    private final SaveCoursePinPort savePinPort = mock(SaveCoursePinPort.class);
    private final DeleteCoursePinPort deletePinPort = mock(DeleteCoursePinPort.class);

    private final SetStepStatusService statusService = new SetStepStatusService(loadStepPort, saveStepStatusPort);
    private final PinCourseService pinService =
            new PinCourseService(loadStepPort, userCoursesPort, loadPinPort, savePinPort);
    private final UnpinCourseService unpinService =
            new UnpinCourseService(loadStepPort, userCoursesPort, loadPinPort, deletePinPort);

    private RoadmapStep located(UUID ownerId) {
        RoadmapStep step = RoadmapStep.withId(UUID.randomUUID(), "Java", 1, null, null, List.of());
        when(loadStepPort.loadStep(step.getId())).thenReturn(Optional.of(new LocatedStep(ROADMAP, ownerId, step)));
        return step;
    }

    @Test
    void statusSoNoPassoDoRoadmapAcessivel() {
        RoadmapStep mine = located(ANA);

        StepResponse response = statusService.setStatus(ANA, ROADMAP, mine.getId(), StepStatus.DONE);

        assertThat(response.status()).isEqualTo("DONE");
        verify(saveStepStatusPort).save(ANA, mine.getId(), StepStatus.DONE);
        assertThatThrownBy(() -> statusService.setStatus(BIA, ROADMAP, mine.getId(), StepStatus.DONE))
                .hasMessage("Roadmap not found");
        assertThatThrownBy(() -> statusService.setStatus(ANA, UUID.randomUUID(), mine.getId(), StepStatus.DONE))
                .hasMessage("Step not found");
    }

    @Test
    void pinRepetidoAtualizaEmVezDeCriar() {
        RoadmapStep step = located(null);
        CoursePin existing = CoursePin.pin(COURSE, step.getId(), "antigo", 1);
        when(userCoursesPort.isOwner(COURSE, ANA)).thenReturn(true);
        when(loadPinPort.loadOn(step.getId(), COURSE)).thenReturn(Optional.of(existing));

        PinResponse response = pinService.pin(new PinCourseCommand(ANA, step.getId(), COURSE, null, 5));

        assertThat(response.id()).isEqualTo(existing.getId());
        assertThat(response.rating()).isEqualTo(5);
        verify(savePinPort).update(existing);
        verify(savePinPort, never()).create(any());
    }

    @Test
    void pinComCursoAlheioNaoGrava() {
        RoadmapStep step = located(null);

        assertThatThrownBy(() -> pinService.pin(new PinCourseCommand(ANA, step.getId(), COURSE, null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course not found");
        verify(savePinPort, never()).create(any());
    }

    @Test
    void desfazerPinDeOutroPassoOuCursoAlheioRetorna404() {
        RoadmapStep step = located(ANA);
        RoadmapStep other = located(ANA);
        CoursePin pin = CoursePin.pin(COURSE, step.getId(), null, null);
        when(loadPinPort.load(pin.getId())).thenReturn(Optional.of(pin));

        assertThatThrownBy(() -> unpinService.unpin(ANA, other.getId(), pin.getId())).hasMessage("Pin not found");
        assertThatThrownBy(() -> unpinService.unpin(ANA, step.getId(), pin.getId())).hasMessage("Pin not found");
        verify(deletePinPort, never()).delete(any());

        when(userCoursesPort.isOwner(COURSE, ANA)).thenReturn(true);
        unpinService.unpin(ANA, step.getId(), pin.getId());
        verify(deletePinPort).delete(pin.getId());
    }

    @Test
    void listaCalculaProgressoESemPassosNaoConsultaStatus() {
        StepNode node = new StepNode(UUID.randomUUID(), null, 1);
        RoadmapOutline withSteps = new RoadmapOutline(ROADMAP, null, "Pronto", null, List.of(node));
        RoadmapOutline empty = new RoadmapOutline(UUID.randomUUID(), ANA, "Meu", null, List.of());
        when(listRoadmapsPort.listOutlinesFor(ANA)).thenReturn(List.of(withSteps, empty));
        when(statusesPort.statusesOf(ANA, List.of(node.id()))).thenReturn(Map.of(node.id(), StepStatus.DONE));
        when(listRoadmapsPort.listOutlinesFor(BIA)).thenReturn(List.of(empty));
        RoadmapQueryService service = new RoadmapQueryService(listRoadmapsPort, mock(LoadRoadmapPort.class),
                statusesPort);

        List<RoadmapResponse> roadmaps = service.list(ANA);

        assertThat(roadmaps.getFirst().predefined()).isTrue();
        assertThat(roadmaps.getFirst().progress().done()).isEqualTo(1);
        assertThat(roadmaps.get(1).progress().total()).isZero();
        service.list(BIA);
        verify(statusesPort, never()).statusesOf(org.mockito.ArgumentMatchers.eq(BIA), anyCollection());
    }
}
