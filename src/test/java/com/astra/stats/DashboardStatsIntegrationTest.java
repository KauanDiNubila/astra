package com.astra.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
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
class DashboardStatsIntegrationTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private MockMvc mockMvc;

    private String newUserToken() throws Exception {
        String email = "stats-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Stats\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.4." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }

    private void session(String token, String categoryId, int daysAgo, int minutes) throws Exception {
        String startedAt = LocalDate.now(ZONE).minusDays(daysAgo).atTime(12, 0).atZone(ZONE).toOffsetDateTime().toString();
        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + categoryId + "\",\"focusedMinutes\":" + minutes
                                + ",\"startedAt\":\"" + startedAt + "\",\"note\":null}"))
                .andExpect(status().isCreated());
    }

    @Test
    void dashboardComparesWithLastWeekAndReportsLongestStreak() throws Exception {
        String token = newUserToken();
        String created = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Leitura\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String categoryId = JsonPath.read(created, "$.id");

        session(token, categoryId, 0, 30);
        session(token, categoryId, 1, 20);
        session(token, categoryId, 2, 10);
        session(token, categoryId, 7, 40);
        for (int daysAgo = 20; daysAgo <= 23; daysAgo++) {
            session(token, categoryId, daysAgo, 15);
        }
        session(token, categoryId, 40, 50);

        mockMvc.perform(get("/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayMinutes").value(30))
                .andExpect(jsonPath("$.currentStreak").value(3))
                .andExpect(jsonPath("$.previousWeekMinutes").value(40))
                .andExpect(jsonPath("$.last30DaysMinutes").value(160))
                .andExpect(jsonPath("$.longestStreak").value(4))
                .andExpect(jsonPath("$.totalMinutes").value(210));
    }

    @Test
    void emptyAccountHasZeroedComparisons() throws Exception {
        String token = newUserToken();
        mockMvc.perform(get("/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousWeekMinutes").value(0))
                .andExpect(jsonPath("$.last30DaysMinutes").value(0))
                .andExpect(jsonPath("$.longestStreak").value(0));
    }

    private String category(String token, String name) throws Exception {
        String created = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private void goal(String token, String type, int hours) throws Exception {
        mockMvc.perform(put("/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"" + type + "\",\"targetHours\":" + hours + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void metasMostramHorasFeitasEQuaisForamAtingidas() throws Exception {
        String token = newUserToken();
        String reading = category(token, "Leitura");
        session(token, reading, 0, 90);
        session(token, reading, 0, 30);
        goal(token, "DAILY", 2);
        goal(token, "WEEKLY", 5);

        mockMvc.perform(get("/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals.length()").value(2))
                .andExpect(jsonPath("$.goals[?(@.type == 'DAILY')].targetHours").value(2))
                .andExpect(jsonPath("$.goals[?(@.type == 'DAILY')].achievedHours").value(2.0))
                .andExpect(jsonPath("$.goals[?(@.type == 'DAILY')].reached").value(true))
                .andExpect(jsonPath("$.goals[?(@.type == 'WEEKLY')].achievedHours").value(2.0))
                .andExpect(jsonPath("$.goals[?(@.type == 'WEEKLY')].reached").value(false));
    }

    @Test
    void heatmapTrazUmDiaPorLinhaDentroDoAno() throws Exception {
        String token = newUserToken();
        String reading = category(token, "Leitura");
        session(token, reading, 0, 30);
        session(token, reading, 0, 15);
        session(token, reading, 40, 60);
        session(token, reading, 400, 99);
        String today = LocalDate.now(ZONE).toString();
        String fortyDaysAgo = LocalDate.now(ZONE).minusDays(40).toString();

        mockMvc.perform(get("/heatmap").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.day == '" + today + "')].minutes").value(45))
                .andExpect(jsonPath("$[?(@.day == '" + fortyDaysAgo + "')].minutes").value(60));
    }

    @Test
    void porCategoriaRespeitaAJanelaDeDias() throws Exception {
        String token = newUserToken();
        String reading = category(token, "Leitura");
        String code = category(token, "Código");
        session(token, reading, 0, 90);
        session(token, code, 1, 30);
        session(token, reading, 40, 60);

        mockMvc.perform(get("/dashboard/by-category").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.categoryId == '" + reading + "')].minutes").value(90))
                .andExpect(jsonPath("$[?(@.categoryId == '" + code + "')].minutes").value(30));

        mockMvc.perform(get("/dashboard/by-category").param("days", "60")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.categoryId == '" + reading + "')].minutes").value(150));
    }

    @Test
    void rankingGlobalDoDiaOrdenaPorMinutosDeHojeComNome() throws Exception {
        String first = newUserToken();
        String second = newUserToken();
        session(first, category(first, "Leitura"), 0, 50);
        String secondCategory = category(second, "Leitura");
        session(second, secondCategory, 0, 20);
        session(second, secondCategory, 1, 100);
        String firstId = JsonPath.read(mockMvc.perform(get("/me").header("Authorization", "Bearer " + first))
                .andReturn().getResponse().getContentAsString(), "$.id");
        String secondId = JsonPath.read(mockMvc.perform(get("/me").header("Authorization", "Bearer " + second))
                .andReturn().getResponse().getContentAsString(), "$.id");

        String ranking = mockMvc.perform(get("/ranking").header("Authorization", "Bearer " + first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + firstId + "')].minutes").value(50))
                .andExpect(jsonPath("$[?(@.userId == '" + firstId + "')].name").value("Stats"))
                .andExpect(jsonPath("$[?(@.userId == '" + secondId + "')].minutes").value(20))
                .andReturn().getResponse().getContentAsString();

        List<Integer> positions = JsonPath.read(ranking, "$[*].position");
        assertThat(positions).isEqualTo(IntStream.rangeClosed(1, positions.size()).boxed().toList());
        List<Integer> firstPosition = JsonPath.read(ranking, "$[?(@.userId == '" + firstId + "')].position");
        List<Integer> secondPosition = JsonPath.read(ranking, "$[?(@.userId == '" + secondId + "')].position");
        assertThat(firstPosition.getFirst()).isLessThan(secondPosition.getFirst());
    }

    @Test
    void periodoOuEscopoInvalidoRetorna400() throws Exception {
        String token = newUserToken();
        mockMvc.perform(get("/ranking").param("period", "YEARLY").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/ranking").param("scope", "WORLD").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/heatmap")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/ranking")).andExpect(status().isUnauthorized());
    }
}
