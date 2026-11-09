package com.astra.roadmap.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.roadmap.domain.CoursePin;
import com.astra.roadmap.domain.LocatedStep;
import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.RoadmapOutline;
import com.astra.roadmap.domain.RoadmapStep;
import com.astra.roadmap.domain.StepResource;
import com.astra.roadmap.domain.StepStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, RoadmapPersistenceAdapter.class, RoadmapMapper.class,
        StepProgressPersistenceAdapter.class, CoursePinPersistenceAdapter.class})
class RoadmapPersistenceAdapterTest {

    @Autowired
    private RoadmapPersistenceAdapter roadmaps;

    @Autowired
    private StepProgressPersistenceAdapter progress;

    @Autowired
    private CoursePinPersistenceAdapter pins;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;

    @BeforeEach
    void statistics() {
        statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    private UUID insertUser() {
        UUID id = UUID.randomUUID();
        entityManager.createNativeQuery(
                        "insert into users (id, name, email, password_hash, tag) values (?, ?, ?, 'x', '0001')")
                .setParameter(1, id)
                .setParameter(2, "user-" + id)
                .setParameter(3, id + "@astra.local")
                .executeUpdate();
        return id;
    }

    private UUID insertCourse(UUID userId) {
        UUID id = UUID.randomUUID();
        entityManager.createNativeQuery("insert into course (id, user_id, title) values (?, ?, 'Curso')")
                .setParameter(1, id)
                .setParameter(2, userId)
                .executeUpdate();
        return id;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Roadmap stored(UUID userId) {
        Roadmap roadmap = Roadmap.create(userId, "Meu", "livro");
        RoadmapStep java = roadmap.addStep("Java", 1, null, "base");
        roadmap.addStep("OOP", 1, java.getId(), null);
        roadmap.addResource(java.getId(), "Docs", "https://docs");
        roadmaps.create(roadmap);
        flushAndClear();
        return roadmap;
    }

    @Test
    void criaECarregaOAgregadoInteiro() {
        UUID user = insertUser();
        Roadmap roadmap = stored(user);

        Roadmap loaded = roadmaps.load(roadmap.getId()).orElseThrow();

        assertThat(loaded.getOwnerId()).isEqualTo(user);
        assertThat(loaded.getSteps()).extracting(RoadmapStep::getTitle).containsExactlyInAnyOrder("Java", "OOP");
        assertThat(loaded.step(roadmap.getSteps().getFirst().getId()).getResources())
                .extracting(StepResource::getLabel).containsExactly("Docs");
    }

    @Test
    void gravaPassoELinksNovosERemoveLink() {
        UUID user = insertUser();
        Roadmap roadmap = stored(user);
        UUID javaId = roadmap.getSteps().getFirst().getId();
        UUID docsId = roadmap.getSteps().getFirst().getResources().getFirst().getId();

        Roadmap loaded = roadmaps.loadOwnedByStep(javaId, user).orElseThrow();
        loaded.addStep("Spring", 2, null, null);
        loaded.addResource(javaId, "Vídeo", "https://video");
        loaded.removeResource(javaId, docsId);
        roadmaps.update(loaded);
        flushAndClear();

        Roadmap reloaded = roadmaps.load(roadmap.getId()).orElseThrow();
        assertThat(reloaded.getSteps()).hasSize(3);
        assertThat(reloaded.step(javaId).getResources()).extracting(StepResource::getLabel).containsExactly("Vídeo");
        assertThat(roadmaps.loadOwnedByStep(javaId, insertUser())).isEmpty();
    }

    @Test
    void listarResumosNaoCarregaLinks() {
        UUID user = insertUser();
        Roadmap roadmap = stored(user);
        stored(insertUser());
        statistics.clear();

        List<RoadmapOutline> outlines = roadmaps.listOutlinesFor(user);

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        assertThat(outlines).anyMatch(o -> o.id().equals(roadmap.getId()) && o.steps().size() == 2);
        assertThat(outlines).allMatch(o -> o.isAccessibleBy(user));
        assertThat(roadmaps.listOwnedBy(user)).extracting(Roadmap::getId).containsExactly(roadmap.getId());
    }

    @Test
    void localizarPassoTrazRoadmapEDonoEmDuasConsultas() {
        UUID user = insertUser();
        Roadmap roadmap = stored(user);
        UUID javaId = roadmap.getSteps().getFirst().getId();
        statistics.clear();

        LocatedStep located = roadmaps.loadStep(javaId).orElseThrow();

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        assertThat(located.roadmapId()).isEqualTo(roadmap.getId());
        assertThat(located.ownerId()).isEqualTo(user);
        assertThat(located.step().getResources()).hasSize(1);
        assertThat(roadmaps.loadStep(UUID.randomUUID())).isEmpty();
    }

    @Test
    void statusTrocaRegravaELimpa() {
        UUID user = insertUser();
        UUID stepId = stored(user).getSteps().getFirst().getId();

        progress.save(user, stepId, StepStatus.LEARNING);
        flushAndClear();
        progress.save(user, stepId, StepStatus.DONE);
        flushAndClear();

        assertThat(progress.statusesOf(user, List.of(stepId))).isEqualTo(Map.of(stepId, StepStatus.DONE));
        assertThat(progress.listByUser(user)).singleElement().satisfies(p -> assertThat(p.completedAt()).isNotNull());

        progress.save(user, stepId, null);
        flushAndClear();
        assertThat(progress.statusesOf(user, List.of(stepId))).isEmpty();
    }

    @Test
    void pinCriaAtualizaEApaga() {
        UUID user = insertUser();
        UUID courseId = insertCourse(user);
        UUID stepId = stored(user).getSteps().getFirst().getId();
        CoursePin pin = CoursePin.pin(courseId, stepId, "usando", 4);

        pins.create(pin);
        flushAndClear();
        CoursePin loaded = pins.loadOn(stepId, courseId).orElseThrow();
        loaded.change(null, 5);
        pins.update(loaded);
        flushAndClear();

        assertThat(pins.load(pin.getId()).orElseThrow().getRating()).isEqualTo(5);
        assertThat(pins.listOn(List.of(stepId), List.of(courseId))).hasSize(1);
        assertThat(pins.listForCourses(List.of(courseId))).hasSize(1);

        pins.delete(pin.getId());
        flushAndClear();
        assertThat(pins.load(pin.getId())).isEmpty();
    }
}
