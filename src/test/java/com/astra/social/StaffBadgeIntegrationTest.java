package com.astra.social;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class StaffBadgeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private record TestUser(String token, String id, String handle) {
    }

    private TestUser newUser(String role) throws Exception {
        String name = "S" + UUID.randomUUID().toString().substring(0, 8);
        String email = "staff-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        entityManager.flush();
        jdbcTemplate.update("update users set role = ? where email = ?", role, email);
        entityManager.clear();
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.6." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");
        String me = mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return new TestUser(token, JsonPath.read(me, "$.id"), name + "#" + JsonPath.read(me, "$.tag"));
    }

    private void befriend(TestUser from, TestUser to) throws Exception {
        String sent = mockMvc.perform(post("/friends")
                        .header("Authorization", "Bearer " + from.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + to.handle() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/friends/{id}/accept", (String) JsonPath.read(sent, "$.id"))
                        .header("Authorization", "Bearer " + to.token()))
                .andExpect(status().isOk());
    }

    private void studyNow(TestUser who) throws Exception {
        String category = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + who.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat-" + UUID.randomUUID() + "\"}"))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + who.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + JsonPath.read(category, "$.id")
                                + "\",\"focusedMinutes\":30,\"startedAt\":\"" + OffsetDateTime.now() + "\"}"))
                .andExpect(status().isCreated());
    }

    private String byId(String id) {
        return "[?(@.friendUserId == '" + id + "')]";
    }

    @Test
    void donoEAdminTemSeloNaListaDeAmigosEUsuarioComumNao() throws Exception {
        TestUser viewer = newUser("USER");
        TestUser owner = newUser("OWNER");
        TestUser admin = newUser("ADMIN");
        TestUser regular = newUser("USER");
        befriend(viewer, owner);
        befriend(viewer, admin);
        befriend(viewer, regular);

        mockMvc.perform(get("/friends").header("Authorization", "Bearer " + viewer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$" + byId(owner.id()) + ".friendAdmin").value(true))
                .andExpect(jsonPath("$" + byId(admin.id()) + ".friendAdmin").value(true))
                .andExpect(jsonPath("$" + byId(regular.id()) + ".friendAdmin").value(false));
    }

    @Test
    void donoEAdminTemSeloNoRanking() throws Exception {
        TestUser viewer = newUser("USER");
        TestUser owner = newUser("OWNER");
        TestUser admin = newUser("ADMIN");
        befriend(viewer, owner);
        befriend(viewer, admin);
        studyNow(viewer);
        studyNow(owner);
        studyNow(admin);

        mockMvc.perform(get("/ranking").param("scope", "FRIENDS")
                        .header("Authorization", "Bearer " + viewer.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + owner.id() + "')].admin").value(true))
                .andExpect(jsonPath("$[?(@.userId == '" + admin.id() + "')].admin").value(true))
                .andExpect(jsonPath("$[?(@.userId == '" + viewer.id() + "')].admin").value(false));
    }
}
