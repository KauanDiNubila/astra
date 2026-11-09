package com.astra.learning.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.learning.domain.Course;
import com.astra.learning.domain.CourseModule;
import com.astra.learning.domain.Goal;
import com.astra.learning.domain.GoalType;
import com.astra.learning.domain.Lesson;
import jakarta.persistence.EntityManager;
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
@Import({TestcontainersConfiguration.class, CoursePersistenceAdapter.class, CourseMapper.class,
        GoalPersistenceAdapter.class})
class CoursePersistenceAdapterTest {

    @Autowired
    private CoursePersistenceAdapter adapter;

    @Autowired
    private GoalPersistenceAdapter goals;

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

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Course stored(UUID userId) {
        Course course = Course.plan(userId, "Spring", "Udemy", 0);
        course.addModule("A", 1, 3);
        course.addModule("B", 2, 0);
        adapter.create(course);
        flushAndClear();
        return course;
    }

    @Test
    void criaComModulosEAulasSoComInserts() {
        UUID user = insertUser();
        Course course = Course.plan(user, "Spring", "Udemy", 2);
        course.addModule("Extra", 3, 2);
        statistics.clear();

        adapter.create(course);
        entityManager.flush();

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1 + 3 + 2);
        entityManager.clear();
        Course loaded = adapter.loadOwned(course.getId(), user).orElseThrow();
        assertThat(loaded.getModules()).extracting(CourseModule::getTitle)
                .containsExactly("Módulo 1", "Módulo 2", "Extra");
        assertThat(loaded.getModules().get(2).getLessons()).extracting(Lesson::getTitle)
                .containsExactly("Aula 1", "Aula 2");
    }

    @Test
    void carregaSoDoDono() {
        UUID user = insertUser();
        Course course = stored(user);

        assertThat(adapter.loadOwned(course.getId(), user)).isPresent();
        assertThat(adapter.loadOwned(course.getId(), insertUser())).isEmpty();
        assertThat(adapter.isOwner(course.getId(), user)).isTrue();
        assertThat(adapter.courseIdsOf(user)).containsExactly(course.getId());
    }

    @Test
    void marcarAulaCustaTresLeiturasEUmaEscrita() {
        UUID user = insertUser();
        Course course = stored(user);
        UUID moduleId = course.getModules().getFirst().getId();
        UUID lessonId = course.getModules().getFirst().getLessons().getFirst().getId();
        statistics.clear();

        Course loaded = adapter.loadOwned(course.getId(), user).orElseThrow();
        loaded.markLessonCompleted(moduleId, lessonId, true);
        adapter.update(loaded);
        entityManager.flush();

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3 + 1);
        entityManager.clear();
        assertThat(adapter.loadOwned(course.getId(), user).orElseThrow().completedLessons()).isEqualTo(1);
    }

    @Test
    void gravaAulaNovaRemovidaERenumeradaEModuloNovo() {
        UUID user = insertUser();
        Course course = stored(user);
        UUID moduleId = course.getModules().getFirst().getId();

        Course loaded = adapter.loadOwned(course.getId(), user).orElseThrow();
        loaded.removeLesson(moduleId, loaded.module(moduleId).getLessons().getFirst().getId());
        loaded.addLesson(moduleId, "Nova", 3);
        loaded.renameModule(moduleId, "Renomeado");
        CourseModule added = loaded.addModule("C", 3, 1);
        adapter.update(loaded);
        flushAndClear();

        Course reloaded = adapter.loadOwned(course.getId(), user).orElseThrow();
        assertThat(reloaded.module(moduleId).getTitle()).isEqualTo("Renomeado");
        assertThat(reloaded.module(moduleId).getLessons()).extracting(Lesson::getTitle)
                .containsExactly("Aula 2", "Aula 3", "Nova");
        assertThat(reloaded.module(moduleId).getLessons()).extracting(Lesson::getPosition)
                .containsExactly(1, 2, 3);
        assertThat(reloaded.module(added.getId()).totalLessons()).isEqualTo(1);
        assertThat(reloaded.getModules()).hasSize(3);
    }

    @Test
    void listarTrazTodosOsCursosEmTresConsultas() {
        UUID user = insertUser();
        stored(user);
        stored(user);
        stored(insertUser());
        statistics.clear();

        assertThat(adapter.listByUser(user)).hasSize(2).allSatisfy(c -> assertThat(c.totalLessons()).isEqualTo(3));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }

    @Test
    void metaDefinidaDeNovoAtualizaEmVezDeDuplicar() {
        UUID user = insertUser();

        goals.save(new Goal(user, GoalType.DAILY, 2));
        goals.save(new Goal(user, GoalType.DAILY, 3));
        goals.save(new Goal(user, GoalType.WEEKLY, 10));
        flushAndClear();

        assertThat(goals.loadByUser(user)).containsExactlyInAnyOrder(
                new Goal(user, GoalType.DAILY, 3), new Goal(user, GoalType.WEEKLY, 10));
    }
}
