package com.astra.roadmap.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astra.shared.exception.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RoadmapTest {

    private static final UUID USER = UUID.randomUUID();

    @Test
    void roadmapProprioComPassosEmOrdemEPaiDoMesmoRoadmap() {
        Roadmap roadmap = Roadmap.create(USER, "Backend", "livro");
        RoadmapStep spring = roadmap.addStep("Spring", 2, null, null);
        RoadmapStep java = roadmap.addStep("Java", 1, null, "base");
        RoadmapStep oop = roadmap.addStep("OOP", 1, java.getId(), null);

        assertThat(roadmap.getSteps()).extracting(RoadmapStep::getTitle).containsExactly("Java", "OOP", "Spring");
        assertThat(oop.getParentStepId()).isEqualTo(java.getId());
        assertThat(roadmap.outline().isPredefined()).isFalse();
        assertThat(roadmap.outline().stepIds()).contains(spring.getId());

        Roadmap other = Roadmap.create(USER, "Outro", null);
        assertThatThrownBy(() -> other.addStep("X", 1, java.getId(), null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Parent step not found");
        assertThat(other.getSteps()).isEmpty();
    }

    @Test
    void linksGanhamProximaPosicaoESaemSemErroSeNaoExistirem() {
        Roadmap roadmap = Roadmap.create(USER, "Backend", null);
        RoadmapStep step = roadmap.addStep("Java", 1, null, null);

        StepResource docs = roadmap.addResource(step.getId(), "Docs", "https://docs");
        StepResource video = roadmap.addResource(step.getId(), "Vídeo", "https://video");
        roadmap.removeResource(step.getId(), UUID.randomUUID());

        assertThat(docs.getPosition()).isZero();
        assertThat(video.getPosition()).isEqualTo(1);
        roadmap.removeResource(step.getId(), docs.getId());
        assertThat(step.getResources()).containsExactly(video);
        assertThatThrownBy(() -> roadmap.addResource(UUID.randomUUID(), "X", "https://x"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Step not found");
    }

    @Test
    void acessoProntoParaTodosEProprioSoParaODono() {
        RoadmapOutline predefined = new RoadmapOutline(UUID.randomUUID(), null, "Pronto", null, List.of());
        RoadmapOutline mine = Roadmap.create(USER, "Meu", null).outline();

        assertThat(predefined.isAccessibleBy(USER)).isTrue();
        assertThat(mine.isAccessibleBy(USER)).isTrue();
        assertThat(mine.isAccessibleBy(UUID.randomUUID())).isFalse();
        UUID roadmapId = UUID.randomUUID();
        RoadmapStep step = RoadmapStep.withId(UUID.randomUUID(), "X", 1, null, null, List.of());
        assertThat(new LocatedStep(roadmapId, null, step).isAccessibleBy(USER)).isTrue();
        assertThat(new LocatedStep(roadmapId, UUID.randomUUID(), step).isAccessibleBy(USER)).isFalse();
        assertThat(new LocatedStep(roadmapId, USER, step).isIn(roadmapId)).isTrue();
    }

    @Test
    void progressoContaMarcosTopicosEEstados() {
        StepNode java = node(null, 1);
        StepNode oop = node(java.id(), 1);
        StepNode streams = node(java.id(), 2);
        StepNode spring = node(null, 2);
        StepNode mvc = node(spring.id(), 1);
        StepNode security = node(spring.id(), 2);
        StepNode docker = node(null, 3);
        StepNode compose = node(docker.id(), 1);
        Map<UUID, StepStatus> statuses = new HashMap<>();
        statuses.put(oop.id(), StepStatus.DONE);
        statuses.put(streams.id(), StepStatus.DONE);
        statuses.put(mvc.id(), StepStatus.LEARNING);
        statuses.put(compose.id(), StepStatus.SKIPPED);

        RoadmapProgress progress = RoadmapProgress.of(
                List.of(docker, compose, java, oop, streams, spring, mvc, security), statuses);

        assertThat(progress).isEqualTo(new RoadmapProgress(3, 5, 2, 7, true,
                List.of(MilestoneState.DONE, MilestoneState.ACTIVE, MilestoneState.TODO)));
    }

    @Test
    void marcoFeitoPeloProprioStatusEMarcoComFilhosPulados() {
        StepNode milestone = node(null, 1);
        StepNode skipped = node(milestone.id(), 1);
        StepNode lone = node(null, 2);

        assertThat(RoadmapProgress.of(List.of(milestone, skipped, lone),
                Map.of(skipped.id(), StepStatus.SKIPPED, lone.id(), StepStatus.DONE)).milestoneStates())
                .containsExactly(MilestoneState.TODO, MilestoneState.DONE);
        assertThat(RoadmapProgress.of(List.of(), Map.of()))
                .isEqualTo(new RoadmapProgress(0, 0, 0, 0, false, List.of()));
    }

    @Test
    void pinMudaStatusENota() {
        UUID step = UUID.randomUUID();
        CoursePin pin = CoursePin.pin(UUID.randomUUID(), step, "usando", 4);

        pin.change(null, 5);

        assertThat(pin.getStatus()).isNull();
        assertThat(pin.getRating()).isEqualTo(5);
        assertThat(pin.isOn(step)).isTrue();
        assertThat(pin.isOn(UUID.randomUUID())).isFalse();
    }

    private static StepNode node(UUID parent, int position) {
        return new StepNode(UUID.randomUUID(), parent, position);
    }
}
