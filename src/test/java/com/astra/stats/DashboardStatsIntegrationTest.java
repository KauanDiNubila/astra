package com.astra.stats;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.ZoneId;
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
}
