package com.astra.github.adapter.out.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.github.domain.GitHubApiException;
import com.astra.shared.crypto.EncryptionService;
import com.astra.shared.crypto.JwtService;
import com.jayway.jsonpath.JsonPath;
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
class GitHubSyncErrorIntegrationTest {

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

    private String connectedUser(String encryptedAccessToken) throws Exception {
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sync\",\"email\":\"gh-sync-" + UUID.randomUUID()
                                + "@astra.local\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        userId = UUID.fromString(JsonPath.read(registered, "$.id"));
        jdbcTemplate.update("insert into github_connection (user_id, github_user_id, github_login, access_token, "
                + "access_token_expires_at) values (?, 1, 'octocat', ?, now() + interval '1 day')",
                userId, encryptedAccessToken);
        return "Bearer " + jwtService.generateToken(userId);
    }

    @Test
    void falhaDaApiNaSincronizacaoFicaRegistradaNoStatus() throws Exception {
        String bearer = connectedUser(encryptionService.encrypt("token"));
        when(gitHubApiClient.graphql(anyString(), anyString(), any()))
                .thenThrow(new GitHubApiException("GitHub respondeu 502 na consulta GraphQL"));

        mockMvc.perform(post("/github/sync").header("Authorization", bearer))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get("/github/status").header("Authorization", bearer))
                .andExpect(jsonPath("$.lastSyncError").value("GitHub respondeu 502 na consulta GraphQL"))
                .andExpect(jsonPath("$.lastSyncedAt").exists());
    }

    @Test
    void tokenCorrompidoFicaRegistradoParaAPessoaReconectar() throws Exception {
        String bearer = connectedUser("isto-nao-e-um-token-cifrado");

        mockMvc.perform(post("/github/sync").header("Authorization", bearer))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/github/status").header("Authorization", bearer))
                .andExpect(jsonPath("$.lastSyncError").value("Conexão inválida — reconecte sua conta do GitHub"));
    }
}
