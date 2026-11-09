package com.astra.tracking.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.CategoryMinutes;
import com.astra.tracking.session.application.port.in.GetFocusedMinutesQuery.DailyMinutes;
import com.astra.tracking.session.application.port.in.GetLastStudiedQuery;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery;
import com.astra.tracking.session.application.port.in.GetMinutesByUserQuery.UserMinutes;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class SessionStatsIntegrationTest {

    private static final OffsetDateTime START = OffsetDateTime.parse("2099-01-10T00:00:00-03:00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GetFocusedMinutesQuery getFocusedMinutesQuery;

    @Autowired
    private GetMinutesByUserQuery getMinutesByUserQuery;

    @Autowired
    private GetLastStudiedQuery getLastStudiedQuery;

    private record TestUser(UUID id, String token) {
    }

    private TestUser newUser() throws Exception {
        String email = "stats-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.2." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");
        String me = mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new TestUser(UUID.fromString(JsonPath.read(me, "$.id")), token);
    }

    private UUID createCategory(TestUser user) throws Exception {
        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat-" + UUID.randomUUID() + "\",\"color\":\"#4C8BF5\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private UUID createCourse(TestUser user) throws Exception {
        String body = mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Curso\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private void session(TestUser user, UUID categoryId, UUID courseId, int minutes, String startedAt) throws Exception {
        String course = courseId == null ? "" : ",\"courseId\":\"" + courseId + "\"";
        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + categoryId + "\"" + course
                                + ",\"focusedMinutes\":" + minutes + ",\"startedAt\":\"" + startedAt + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void somaOsMinutosNoTotalEDesdeUmaData() throws Exception {
        TestUser user = newUser();
        UUID category = createCategory(user);
        session(user, category, null, 40, "2099-01-09T12:00:00-03:00");
        session(user, category, null, 25, "2099-01-10T12:00:00-03:00");
        session(user, category, null, 15, "2099-01-11T12:00:00-03:00");

        assertThat(getFocusedMinutesQuery.total(user.id())).isEqualTo(80);
        assertThat(getFocusedMinutesQuery.since(user.id(), START)).isEqualTo(40);
    }

    @Test
    void usuarioSemSessaoTemZeroMinutos() throws Exception {
        TestUser user = newUser();

        assertThat(getFocusedMinutesQuery.total(user.id())).isZero();
        assertThat(getFocusedMinutesQuery.since(user.id(), START)).isZero();
    }

    @Test
    void agrupaPorDiaNoHorarioDeSaoPaulo() throws Exception {
        TestUser user = newUser();
        UUID category = createCategory(user);
        session(user, category, null, 30, "2099-01-10T23:30:00-03:00");
        session(user, category, null, 20, "2099-01-11T01:00:00Z");
        session(user, category, null, 10, "2099-01-11T09:00:00-03:00");

        assertThat(getFocusedMinutesQuery.dailySince(user.id(), START)).containsExactly(
                new DailyMinutes(LocalDate.parse("2099-01-10"), 50),
                new DailyMinutes(LocalDate.parse("2099-01-11"), 10));
    }

    @Test
    void agrupaPorCategoria() throws Exception {
        TestUser user = newUser();
        UUID estudo = createCategory(user);
        UUID trabalho = createCategory(user);
        session(user, estudo, null, 30, "2099-01-10T10:00:00-03:00");
        session(user, estudo, null, 20, "2099-01-10T14:00:00-03:00");
        session(user, trabalho, null, 45, "2099-01-11T10:00:00-03:00");
        session(user, trabalho, null, 99, "2099-01-09T10:00:00-03:00");

        assertThat(getFocusedMinutesQuery.byCategorySince(user.id(), START)).containsExactlyInAnyOrder(
                new CategoryMinutes(estudo, 50),
                new CategoryMinutes(trabalho, 45));
    }

    @Test
    void rankingOrdenaPorMinutosEIgnoraSessoesAntesDoInicio() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        TestUser caio = newUser();
        session(ana, createCategory(ana), null, 30, "2099-01-10T10:00:00-03:00");
        UUID biaCategory = createCategory(bia);
        session(bia, biaCategory, null, 50, "2099-01-10T10:00:00-03:00");
        session(bia, biaCategory, null, 20, "2099-01-11T10:00:00-03:00");
        session(caio, createCategory(caio), null, 500, "2099-01-09T10:00:00-03:00");

        List<UserMinutes> ranking = getMinutesByUserQuery.since(START).stream()
                .filter(r -> Set.of(ana.id(), bia.id(), caio.id()).contains(r.userId()))
                .toList();

        assertThat(ranking).containsExactly(new UserMinutes(bia.id(), 70), new UserMinutes(ana.id(), 30));
    }

    @Test
    void rankingPorUsuariosConsideraSoOsInformados() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        TestUser caio = newUser();
        session(ana, createCategory(ana), null, 30, "2099-01-10T10:00:00-03:00");
        session(bia, createCategory(bia), null, 50, "2099-01-10T10:00:00-03:00");
        session(caio, createCategory(caio), null, 90, "2099-01-10T10:00:00-03:00");

        assertThat(getMinutesByUserQuery.sinceForUsers(START, Set.of(ana.id(), bia.id())))
                .containsExactly(new UserMinutes(bia.id(), 50), new UserMinutes(ana.id(), 30));
        assertThat(getMinutesByUserQuery.sinceForUsers(START, Set.of())).isEmpty();
    }

    @Test
    void ultimoEstudoPorCurso() throws Exception {
        TestUser user = newUser();
        UUID category = createCategory(user);
        UUID estudado = createCourse(user);
        UUID intocado = createCourse(user);
        session(user, category, estudado, 30, "2099-01-10T10:00:00-03:00");
        session(user, category, estudado, 30, "2099-01-12T10:00:00-03:00");

        var lastStudied = getLastStudiedQuery.byCourse(user.id(), Set.of(estudado, intocado));

        assertThat(lastStudied).containsOnlyKeys(estudado);
        assertThat(lastStudied.get(estudado).toInstant())
                .isEqualTo(OffsetDateTime.parse("2099-01-12T10:00:00-03:00").toInstant());
        assertThat(getLastStudiedQuery.byCourse(user.id(), Set.of())).isEmpty();
    }
}
