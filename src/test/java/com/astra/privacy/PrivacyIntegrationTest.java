package com.astra.privacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.shared.crypto.JwtService;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
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
class PrivacyIntegrationTest {

    private static final String PASSWORD = "Xk9$mQ2vN8pL4wR7";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private boolean exists(UUID userId) {
        entityManager.flush();
        return jdbcTemplate.queryForObject("select count(*) from users where id = ?", Integer.class, userId) == 1;
    }

    private UUID register(boolean acceptTerms) throws Exception {
        String email = "privacy-" + UUID.randomUUID() + "@astra.local";
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Priv\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD
                                + "\",\"acceptTerms\":" + acceptTerms + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private String bearer(UUID userId) {
        return "Bearer " + jwtService.generateToken(userId);
    }

    @Test
    void registeringWithAcceptanceRecordsTheTerms() throws Exception {
        UUID userId = register(true);

        mockMvc.perform(get("/me").header("Authorization", bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termsAccepted").value(true))
                .andExpect(jsonPath("$.hasPassword").value(true));
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject("select terms_accepted_at from users where id = ?",
                java.time.OffsetDateTime.class, userId)).isNotNull();
    }

    @Test
    void existingUserWithoutAcceptanceCanAcceptLater() throws Exception {
        UUID userId = register(false);

        mockMvc.perform(get("/me").header("Authorization", bearer(userId)))
                .andExpect(jsonPath("$.termsAccepted").value(false));

        mockMvc.perform(post("/me/terms").header("Authorization", bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termsAccepted").value(true));
    }

    @Test
    void exportReturnsTheUsersOwnData() throws Exception {
        UUID userId = register(true);
        String category = mockMvc.perform(post("/categories")
                        .header("Authorization", bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Estudo\"}"))
                .andReturn().getResponse().getContentAsString();
        String categoryId = JsonPath.read(category, "$.id");
        mockMvc.perform(post("/sessions")
                        .header("Authorization", bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + categoryId
                                + "\",\"focusedMinutes\":42,\"startedAt\":\"2026-10-01T10:00:00Z\",\"note\":\"revisão\"}"))
                .andExpect(status().isCreated());

        UUID otherId = register(true);
        mockMvc.perform(post("/categories")
                        .header("Authorization", bearer(otherId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alheia\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/me/export").header("Authorization", bearer(userId)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(jsonPath("$.profile.id").value(userId.toString()))
                .andExpect(jsonPath("$.profile.termsVersion").exists())
                .andExpect(jsonPath("$.categories", hasSize(1)))
                .andExpect(jsonPath("$.categories[0].name").value("Estudo"))
                .andExpect(jsonPath("$.sessions", hasSize(1)))
                .andExpect(jsonPath("$.sessions[0].focusedMinutes").value(42))
                .andExpect(jsonPath("$.sessions[0].note").value("revisão"))
                .andExpect(jsonPath("$.messages", hasSize(0)))
                .andExpect(jsonPath("$.friendships", hasSize(0)));
    }

    @Test
    void exportRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/me/export")).andExpect(status().isUnauthorized());
    }

    @Test
    void deletingTheAccountRequiresThePassword() throws Exception {
        UUID userId = register(true);

        mockMvc.perform(delete("/me")
                        .header("Authorization", bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"senha-errada\"}"))
                .andExpect(status().isConflict());
        assertThat(exists(userId)).isTrue();

        mockMvc.perform(delete("/me")
                        .header("Authorization", bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());
        assertThat(exists(userId)).isFalse();
    }

    @Test
    void accountWithoutPasswordConfirmsByTypingTheEmail() throws Exception {
        String email = "oauth-" + UUID.randomUUID() + "@astra.local";
        UUID oauthUserId = UUID.randomUUID();
        jdbcTemplate.update("insert into users (id, name, email, tag) values (?, 'OAuth', ?, '1234')",
                oauthUserId, email);

        mockMvc.perform(delete("/me")
                        .header("Authorization", bearer(oauthUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmation\":\"outro@astra.local\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/me")
                        .header("Authorization", bearer(oauthUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmation\":\" " + email.toUpperCase() + " \"}"))
                .andExpect(status().isNoContent());
        assertThat(exists(oauthUserId)).isFalse();
    }

    @Test
    void ownerCannotDeleteTheirOwnAccount() throws Exception {
        UUID userId = register(true);
        entityManager.flush();
        jdbcTemplate.update("update users set role = 'OWNER' where id = ?", userId);
        entityManager.clear();

        mockMvc.perform(delete("/me")
                        .header("Authorization", bearer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isConflict());
        assertThat(exists(userId)).isTrue();
    }
}
