package com.astra.tracking.session.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.TestcontainersConfiguration;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.CategoryMinutes;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.DailyMinutes;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery.UserMinutes;
import com.astra.tracking.session.domain.Session;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, SessionPersistenceAdapter.class, SessionMapper.class})
class SessionPersistenceAdapterTest {

    private static final OffsetDateTime START = OffsetDateTime.parse("2099-01-10T00:00:00-03:00");

    @Autowired
    private SessionPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

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

    private UUID insertCategory(UUID userId) {
        UUID id = UUID.randomUUID();
        entityManager.createNativeQuery("insert into category (id, user_id, name) values (?, ?, ?)")
                .setParameter(1, id)
                .setParameter(2, userId)
                .setParameter(3, "Cat-" + id)
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

    private Session create(UUID userId, UUID categoryId, UUID courseId, int minutes, String startedAt) {
        return adapter.create(Session.start(userId, categoryId, courseId, minutes, OffsetDateTime.parse(startedAt),
                "nota"));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void criaECarregaComTodosOsCampos() {
        UUID user = insertUser();
        UUID category = insertCategory(user);
        UUID course = insertCourse(user);

        Session created = create(user, category, course, 45, "2026-08-03T14:00:00-03:00");
        flushAndClear();
        Session loaded = adapter.load(created.getId()).orElseThrow();

        assertThat(created.getId()).isNotNull();
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUserId()).isEqualTo(user);
        assertThat(loaded.getCategoryId()).isEqualTo(category);
        assertThat(loaded.getCourseId()).isEqualTo(course);
        assertThat(loaded.getFocusedMinutes()).isEqualTo(45);
        assertThat(loaded.getStartedAt().toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-08-03T14:00:00-03:00").toInstant());
        assertThat(loaded.getNote()).isEqualTo("nota");
        assertThat(loaded.getGithubRepositoryId()).isNull();
    }

    @Test
    void carregarSessaoInexistenteDevolveVazio() {
        assertThat(adapter.load(UUID.randomUUID())).isEmpty();
    }

    @Test
    void listaDaMaisNovaParaAMaisAntigaPaginado() {
        UUID user = insertUser();
        UUID category = insertCategory(user);
        create(user, category, null, 10, "2026-08-01T10:00:00-03:00");
        create(user, category, null, 30, "2026-08-03T10:00:00-03:00");
        create(user, category, null, 20, "2026-08-02T10:00:00-03:00");
        create(insertUser(), insertCategory(insertUser()), null, 99, "2026-08-04T10:00:00-03:00");
        flushAndClear();

        assertThat(adapter.listNewestFirst(user, 0, 2)).extracting(Session::getFocusedMinutes).containsExactly(30, 20);
        assertThat(adapter.listNewestFirst(user, 1, 2)).extracting(Session::getFocusedMinutes).containsExactly(10);
    }

    @Test
    void gravaSoORepositorioVinculado() {
        UUID user = insertUser();
        Session session = create(user, insertCategory(user), null, 30, "2026-08-03T10:00:00-03:00");
        UUID repository = UUID.randomUUID();
        entityManager.createNativeQuery("""
                        insert into github_repository (id, user_id, github_repo_id, owner, name, full_name, html_url,
                        private, star_count, fork_count, recent_commit_count, synced_at)
                        values (?, ?, 1, 'dono', 'repo', 'dono/repo', 'https://github.com/dono/repo', false, 0, 0, 0, now())
                        """)
                .setParameter(1, repository)
                .setParameter(2, user)
                .executeUpdate();
        flushAndClear();

        Session loaded = adapter.load(session.getId()).orElseThrow();
        loaded.linkRepository(repository);
        adapter.updateRepository(loaded);
        flushAndClear();
        assertThat(adapter.load(session.getId()).orElseThrow().getGithubRepositoryId()).isEqualTo(repository);

        Session linked = adapter.load(session.getId()).orElseThrow();
        linked.unlinkRepository();
        adapter.updateRepository(linked);
        flushAndClear();
        assertThat(adapter.load(session.getId()).orElseThrow().getGithubRepositoryId()).isNull();
    }

    @Test
    void apagaSessao() {
        UUID user = insertUser();
        Session session = create(user, insertCategory(user), null, 30, "2026-08-03T10:00:00-03:00");
        flushAndClear();

        adapter.delete(session.getId());
        flushAndClear();

        assertThat(adapter.load(session.getId())).isEmpty();
    }

    @Test
    void somasEAgrupamentos() {
        UUID user = insertUser();
        UUID estudo = insertCategory(user);
        UUID trabalho = insertCategory(user);
        create(user, estudo, null, 40, "2099-01-09T12:00:00-03:00");
        create(user, estudo, null, 30, "2099-01-10T23:30:00-03:00");
        create(user, trabalho, null, 20, "2099-01-11T01:00:00Z");
        create(user, trabalho, null, 10, "2099-01-11T09:00:00-03:00");
        flushAndClear();

        assertThat(adapter.sumMinutes(user)).isEqualTo(100);
        assertThat(adapter.sumMinutesSince(user, START)).isEqualTo(60);
        assertThat(adapter.dailyMinutesSince(user, START)).containsExactly(
                new DailyMinutes(LocalDate.parse("2099-01-10"), 50),
                new DailyMinutes(LocalDate.parse("2099-01-11"), 10));
        assertThat(adapter.categoryMinutesSince(user, START)).containsExactlyInAnyOrder(
                new CategoryMinutes(estudo, 30),
                new CategoryMinutes(trabalho, 30));
        assertThat(adapter.existsInCategory(estudo)).isTrue();
        assertThat(adapter.existsInCategory(insertCategory(user))).isFalse();
    }

    @Test
    void minutosPorUsuarioEmOrdemDecrescente() {
        UUID ana = insertUser();
        UUID bia = insertUser();
        UUID caio = insertUser();
        create(ana, insertCategory(ana), null, 30, "2099-01-10T10:00:00-03:00");
        create(bia, insertCategory(bia), null, 70, "2099-01-10T10:00:00-03:00");
        create(caio, insertCategory(caio), null, 500, "2099-01-09T10:00:00-03:00");
        flushAndClear();

        List<UserMinutes> all = adapter.minutesByUserSince(START).stream()
                .filter(m -> Set.of(ana, bia, caio).contains(m.userId()))
                .toList();

        assertThat(all).containsExactly(new UserMinutes(bia, 70), new UserMinutes(ana, 30));
        assertThat(adapter.minutesByUserSince(START, Set.of(ana))).containsExactly(new UserMinutes(ana, 30));
    }

    @Test
    void ultimoEstudoPorCurso() {
        UUID user = insertUser();
        UUID category = insertCategory(user);
        UUID course = insertCourse(user);
        create(user, category, course, 30, "2099-01-10T10:00:00-03:00");
        create(user, category, course, 30, "2099-01-12T10:00:00-03:00");
        flushAndClear();

        assertThat(adapter.lastStudiedByCourse(user, Set.of(course, UUID.randomUUID())))
                .containsOnlyKeys(course)
                .extractingByKey(course)
                .satisfies(last -> assertThat(last.toInstant())
                        .isEqualTo(OffsetDateTime.parse("2099-01-12T10:00:00-03:00").toInstant()));
    }
}
