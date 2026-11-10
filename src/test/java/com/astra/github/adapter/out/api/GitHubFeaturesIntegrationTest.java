package com.astra.github.adapter.out.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.github.domain.TokenGrant;
import com.astra.shared.crypto.EncryptionService;
import com.astra.shared.crypto.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class GitHubFeaturesIntegrationTest {

    private static final String CONTRIBUTIONS = """
            {
              "viewer": {
                "login": "octocat",
                "contributionsCollection": {
                  "totalCommitContributions": %d,
                  "totalPullRequestContributions": 2,
                  "contributionCalendar": { "weeks": [ { "contributionDays": [
                    { "date": "%s", "contributionCount": 3 },
                    { "date": "%s", "contributionCount": 1 }
                  ] } ] },
                  "commitContributionsByRepository": [
                    { "repository": { "databaseId": 999, "name": "astra-backend", "nameWithOwner": "octocat/astra-backend",
                        "owner": { "login": "octocat" }, "description": "backend", "primaryLanguage": { "name": "Java" },
                        "url": "https://github.com/octocat/astra-backend", "isPrivate": false, "stargazerCount": 1,
                        "forkCount": 0, "defaultBranchRef": { "name": "main" }, "pushedAt": "2026-09-02T10:00:00Z",
                        "createdAt": "2025-01-01T10:00:00Z" },
                      "contributions": { "totalCount": %d } },
                    { "repository": { "databaseId": 1000, "name": "site", "nameWithOwner": "octocat/site",
                        "owner": { "login": "octocat" }, "description": null, "primaryLanguage": { "name": "TypeScript" },
                        "url": "https://github.com/octocat/site", "isPrivate": true, "stargazerCount": 0,
                        "forkCount": 0, "defaultBranchRef": null, "pushedAt": null, "createdAt": "2025-01-01T10:00:00Z" },
                      "contributions": { "totalCount": 1 } }
                  ]
                }
              },
              "mergedPRs": { "issueCount": 1 },
              "closedIssues": { "issueCount": 2 }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EncryptionService encryptionService;

    @MockitoBean
    private GitHubApiClient gitHubApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record TestUser(UUID id, String token) {
    }

    private TestUser newUser() throws Exception {
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev\",\"email\":\"gh-" + UUID.randomUUID()
                                + "@astra.local\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(JsonPath.read(body, "$.id"));
        return new TestUser(id, jwtService.generateToken(id));
    }

    private ResultActions call(TestUser user, MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("Authorization", "Bearer " + user.token());
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private void stubContributions(int commits, int backendCommits) throws Exception {
        String today = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString();
        String yesterday = java.time.LocalDate.now(java.time.ZoneOffset.UTC).minusDays(1).toString();
        when(gitHubApiClient.graphql(anyString(), anyString(), any())).thenReturn(objectMapper.readTree(
                CONTRIBUTIONS.formatted(commits, yesterday, today, backendCommits)));
    }

    private void connect(TestUser user, String login) throws Exception {
        when(gitHubApiClient.exchangeCode(anyString(), anyString()))
                .thenReturn(new TokenGrant("gho_access", "ghr_refresh", 28800L, 15811200L));
        when(gitHubApiClient.fetchViewerProfile(anyString()))
                .thenReturn(Map.of("id", 555, "login", login, "avatar_url", "https://avatars/" + login));
        stubContributions(7, 7);
        String url = JsonPath.read(call(user, get("/github/connect/authorize-url"), null)
                .andReturn().getResponse().getContentAsString(), "$.url");
        String state = URLDecoder.decode(URI.create(url).getQuery().split("state=")[1], StandardCharsets.UTF_8);
        assertThat(url).startsWith("https://github.com/login/oauth/authorize?client_id=").contains("scope=public_repo");
        mockMvc.perform(get("/github/connect/callback").param("code", "codigo").param("state", state))
                .andExpect(status().is3xxRedirection());
    }

    private void allowNextSync(TestUser user) {
        entityManager.flush();
        jdbcTemplate.update("update github_connection set last_synced_at = now() - interval '5 minutes' "
                + "where user_id = ?", user.id());
        entityManager.clear();
    }

    @Test
    void sincronizacaoTrazSerieDiariaLinguagensEPeriodos() throws Exception {
        TestUser user = newUser();
        connect(user, "octocat");

        call(user, get("/github/insights"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.repositories.length()").value(2))
                .andExpect(jsonPath("$.repositories[0].fullName").value("octocat/astra-backend"))
                .andExpect(jsonPath("$.repositories[0].recentCommitCount").value(7))
                .andExpect(jsonPath("$.repositories[1].isPrivate").value(true))
                .andExpect(jsonPath("$.languages[0].language").value("Java"))
                .andExpect(jsonPath("$.languages[0].commitCount").value(7))
                .andExpect(jsonPath("$.languages[0].percentage").value(87.5))
                .andExpect(jsonPath("$.series.length()").value(2))
                .andExpect(jsonPath("$.series[1].contributionCount").value(1))
                .andExpect(jsonPath("$.lastSyncedAt").exists());
        call(user, get("/github/activity"), null)
                .andExpect(jsonPath("$.periods[0].period").value("TODAY"))
                .andExpect(jsonPath("$.periods[4].period").value("YEAR"))
                .andExpect(jsonPath("$.periods[4].commitCount").value(7))
                .andExpect(jsonPath("$.periods[4].pullRequestMergedCount").value(1))
                .andExpect(jsonPath("$.periods[4].issueClosedCount").value(2))
                .andExpect(jsonPath("$.periods[4].activeRepoCount").value(2));
    }

    @Test
    void segundaSincronizacaoEsperaEAtualizaOsMesmosRepositorios() throws Exception {
        TestUser user = newUser();
        connect(user, "octocat");

        call(user, post("/github/sync"), null).andExpect(status().isConflict());

        allowNextSync(user);
        stubContributions(20, 19);
        call(user, post("/github/sync"), null).andExpect(status().isNoContent());

        call(user, get("/github/insights"), null)
                .andExpect(jsonPath("$.repositories.length()").value(2))
                .andExpect(jsonPath("$.repositories[0].recentCommitCount").value(19))
                .andExpect(jsonPath("$.series.length()").value(2));
        call(user, get("/github/activity"), null)
                .andExpect(jsonPath("$.periods[4].commitCount").value(20));
    }

    @Test
    void visibilidadeParaAmigosSoComConexao() throws Exception {
        TestUser user = newUser();
        call(user, patch("/github/visibility"), "{\"visibleToFriends\":true}").andExpect(status().isNotFound());

        connect(user, "octocat");
        call(user, get("/github/status"), null).andExpect(jsonPath("$.visibleToFriends").value(false));
        call(user, patch("/github/visibility"), "{\"visibleToFriends\":true}").andExpect(status().isNoContent());
        call(user, get("/github/status"), null)
                .andExpect(jsonPath("$.visibleToFriends").value(true))
                .andExpect(jsonPath("$.avatarUrl").value("https://avatars/octocat"))
                .andExpect(jsonPath("$.connectedAt").exists());
    }

    @Test
    void reconectarAtualizaOLoginELimpaOErro() throws Exception {
        TestUser user = newUser();
        connect(user, "octocat");
        entityManager.flush();
        jdbcTemplate.update("update github_connection set last_sync_error = 'falhou' where user_id = ?", user.id());
        entityManager.clear();

        connect(user, "octocat-novo");

        call(user, get("/github/status"), null)
                .andExpect(jsonPath("$.login").value("octocat-novo"))
                .andExpect(jsonPath("$.lastSyncError").doesNotExist());
    }

    @Test
    void tokenPrestesAVencerERenovadoNaSincronizacao() throws Exception {
        TestUser user = newUser();
        connect(user, "octocat");
        allowNextSync(user);
        jdbcTemplate.update("update github_connection set access_token_expires_at = now() + interval '1 minute' "
                + "where user_id = ?", user.id());
        when(gitHubApiClient.refreshToken("ghr_refresh"))
                .thenReturn(new TokenGrant("gho_novo", "ghr_novo", 28800L, null));

        call(user, post("/github/sync"), null).andExpect(status().isNoContent());

        entityManager.flush();
        Map<String, Object> stored = jdbcTemplate.queryForMap(
                "select access_token, refresh_token, refresh_token_expires_at from github_connection where user_id = ?",
                user.id());
        assertThat(encryptionService.decrypt((String) stored.get("access_token"))).isEqualTo("gho_novo");
        assertThat(encryptionService.decrypt((String) stored.get("refresh_token"))).isEqualTo("ghr_novo");
        assertThat(stored.get("refresh_token_expires_at")).isNull();
    }

    @Test
    void atividadeDaSessaoEscolheORepositorioComMaisCommits() throws Exception {
        TestUser user = newUser();
        String category = call(user, post("/categories"), "{\"name\":\"Código\"}")
                .andReturn().getResponse().getContentAsString();
        String session = call(user, post("/sessions"), "{\"categoryId\":\"" + JsonPath.read(category, "$.id")
                + "\",\"focusedMinutes\":30,\"startedAt\":\"" + OffsetDateTime.now().minusHours(1) + "\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String sessionId = JsonPath.read(session, "$.id");

        call(user, get("/sessions/{id}/github-activity", sessionId), null)
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.commits.length()").value(0));

        connect(user, "octocat");
        doReturn(List.of(Map.of("sha", "abc123", "commit", Map.of("message", "feat: login",
                "author", Map.of("date", "2026-09-02T10:00:00Z")))))
                .when(gitHubApiClient).restGet(anyString(), org.mockito.ArgumentMatchers.contains("astra-backend"), any());
        doReturn(List.of()).when(gitHubApiClient).restGet(anyString(), org.mockito.ArgumentMatchers.contains("/site/"), any());

        call(user, get("/sessions/{id}/github-activity", sessionId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.suggestedRepositoryName").value("octocat/astra-backend"))
                .andExpect(jsonPath("$.commits[0].sha").value("abc123"))
                .andExpect(jsonPath("$.commits[0].message").value("feat: login"))
                .andExpect(jsonPath("$.commits[0].repositoryFullName").value("octocat/astra-backend"));
    }

    @Test
    void evidenciaDoPassoCruzaTituloComNomeOuLinguagem() throws Exception {
        TestUser user = newUser();
        connect(user, "octocat");
        String roadmap = call(user, post("/roadmaps"), "{\"title\":\"Meu\"}").andReturn().getResponse().getContentAsString();
        String javaStep = JsonPath.read(call(user, post("/roadmaps/{id}/steps", (String) JsonPath.read(roadmap, "$.id")),
                "{\"title\":\"Fundamentos de Java\",\"position\":1}").andReturn().getResponse().getContentAsString(), "$.id");
        String otherStep = JsonPath.read(call(user, post("/roadmaps/{id}/steps", (String) JsonPath.read(roadmap, "$.id")),
                "{\"title\":\"Kubernetes\",\"position\":2}").andReturn().getResponse().getContentAsString(), "$.id");

        call(user, get("/steps/{id}/github-evidence", javaStep), null)
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.matchedRepositories.length()").value(1))
                .andExpect(jsonPath("$.matchedRepositories[0].fullName").value("octocat/astra-backend"));
        call(user, get("/steps/{id}/github-evidence", otherStep), null)
                .andExpect(jsonPath("$.matchedRepositories.length()").value(0));
    }

    @Test
    void exportacaoTrazConexaoResumosContribuicoesERepositorios() throws Exception {
        TestUser user = newUser();
        connect(user, "octocat");

        call(user, get("/me/export"), null)
                .andExpect(jsonPath("$.github.login").value("octocat"))
                .andExpect(jsonPath("$.github.activitySummaries.length()").value(5))
                .andExpect(jsonPath("$.github.dailyContributions.length()").value(2))
                .andExpect(jsonPath("$.github.repositories[0].fullName").value("octocat/astra-backend"))
                .andExpect(jsonPath("$.github.repositories[0].recentCommits").value(7));
        call(newUser(), get("/me/export"), null).andExpect(jsonPath("$.github").doesNotExist());
    }
}
