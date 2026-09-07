package com.astra.github;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.github.dto.GitHubTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class GitHubIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitHubApiClient gitHubApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CONTRIBUTIONS_JSON = """
            {
              "viewer": {
                "login": "octocat",
                "contributionsCollection": {
                  "totalCommitContributions": 7,
                  "totalPullRequestContributions": 2,
                  "contributionCalendar": {
                    "weeks": [
                      { "contributionDays": [
                        { "date": "2026-09-01", "contributionCount": 3 },
                        { "date": "2026-09-02", "contributionCount": 0 }
                      ] }
                    ]
                  },
                  "commitContributionsByRepository": [
                    {
                      "repository": {
                        "databaseId": 999,
                        "name": "astra-backend",
                        "nameWithOwner": "octocat/astra-backend",
                        "owner": { "login": "octocat" },
                        "description": "backend",
                        "primaryLanguage": { "name": "Java" },
                        "url": "https://github.com/octocat/astra-backend",
                        "isPrivate": false,
                        "stargazerCount": 1,
                        "forkCount": 0,
                        "defaultBranchRef": { "name": "main" },
                        "pushedAt": "2026-09-02T10:00:00Z",
                        "createdAt": "2025-01-01T10:00:00Z"
                      },
                      "contributions": { "totalCount": 7 }
                    }
                  ]
                }
              },
              "mergedPRs": { "issueCount": 1 },
              "closedIssues": { "issueCount": 2 }
            }
            """;

    private String authToken() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private void stubGitHub() throws Exception {
        when(gitHubApiClient.exchangeCode(anyString(), anyString())).thenReturn(
                new GitHubTokenResponse("gho_access", "ghr_refresh", 28800L, 15811200L));
        when(gitHubApiClient.fetchViewerProfile(anyString())).thenReturn(
                Map.of("id", 555, "login", "octocat", "avatar_url", "https://avatars.example/octocat"));
        JsonNode contributions = objectMapper.readTree(CONTRIBUTIONS_JSON);
        when(gitHubApiClient.graphql(anyString(), anyString(), any())).thenReturn(contributions);
    }

    private void connectGitHub(String token) throws Exception {
        stubGitHub();
        String urlBody = mockMvc.perform(get("/github/connect/authorize-url")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String authorizeUrl = JsonPath.read(urlBody, "$.url");
        String rawState = URI.create(authorizeUrl).getQuery().split("state=")[1];
        String state = URLDecoder.decode(rawState, StandardCharsets.UTF_8);

        mockMvc.perform(get("/github/connect/callback")
                        .param("code", "fake-code")
                        .param("state", state))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void conectaSincronizaEDesconecta() throws Exception {
        String token = authToken();
        connectGitHub(token);

        mockMvc.perform(get("/github/status").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.login").value("octocat"));

        mockMvc.perform(get("/github/activity").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.periods", org.hamcrest.Matchers.hasSize(5)));

        mockMvc.perform(get("/github/insights").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories[0].name").value("astra-backend"))
                .andExpect(jsonPath("$.languages[0].language").value("Java"));

        mockMvc.perform(delete("/github/connection").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/github/status").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void syncSemConexaoRetorna404() throws Exception {
        String token = authToken();
        mockMvc.perform(post("/github/sync").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void callbackComStateInvalidoRedirecionaComErro() throws Exception {
        String redirectedUrl = mockMvc.perform(get("/github/connect/callback")
                        .param("code", "fake-code")
                        .param("state", "not-a-valid-jwt"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        Assertions.assertThat(redirectedUrl).contains("githubError=1");
    }

    @Test
    void statusSemConexaoRetornaDesconectado() throws Exception {
        String token = authToken();
        mockMvc.perform(get("/github/status").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }
}
