package com.astra.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.github.dto.GitHubTokenResponse;
import com.astra.github.service.GitHubApiClient;
import com.astra.shared.crypto.EncryptionService;
import com.astra.shared.crypto.JwtService;
import com.jayway.jsonpath.JsonPath;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class GitHubTokenRefreshIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GitHubApiClient gitHubApiClient;

    private UUID userId;

    @AfterEach
    void cleanUp() {
        if (userId != null) {
            jdbcTemplate.update("delete from users where id = ?", userId);
        }
    }

    private String bearer() {
        return "Bearer " + jwtService.generateToken(userId);
    }

    @Test
    void tokenRenovadoAoVerAtividadeDaSessaoFicaGravado() throws Exception {
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Refresh\",\"email\":\"gh-refresh-" + UUID.randomUUID()
                                + "@astra.local\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        userId = UUID.fromString(JsonPath.read(registered, "$.id"));
        String category = mockMvc.perform(post("/categories").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"GitHub\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String session = mockMvc.perform(post("/sessions").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + JsonPath.read(category, "$.id")
                                + "\",\"focusedMinutes\":30,\"startedAt\":\"" + OffsetDateTime.now().minusHours(1)
                                + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        jdbcTemplate.update("insert into github_connection (user_id, github_user_id, github_login, access_token, "
                        + "refresh_token, access_token_expires_at) values (?, 1, 'octocat', ?, ?, now() + interval '1 minute')",
                userId, encryptionService.encrypt("acesso-velho"), encryptionService.encrypt("refresh-velho"));
        when(gitHubApiClient.refreshToken("refresh-velho"))
                .thenReturn(new GitHubTokenResponse("acesso-novo", "refresh-novo", 28800, null));

        mockMvc.perform(get("/sessions/{id}/github-activity", (String) JsonPath.read(session, "$.id"))
                        .header("Authorization", bearer()))
                .andExpect(status().isOk());

        Map<String, Object> stored = jdbcTemplate.queryForMap(
                "select access_token, refresh_token from github_connection where user_id = ?", userId);
        assertThat(encryptionService.decrypt((String) stored.get("access_token"))).isEqualTo("acesso-novo");
        assertThat(encryptionService.decrypt((String) stored.get("refresh_token"))).isEqualTo("refresh-novo");
    }
}
