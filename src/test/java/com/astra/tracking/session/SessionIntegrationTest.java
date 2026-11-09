package com.astra.tracking.session;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.github.entity.GitHubRepository;
import com.astra.github.repository.GitHubRepositoryRepository;
import com.jayway.jsonpath.JsonPath;
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
class SessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GitHubRepositoryRepository githubRepositoryRepository;

    private String authToken() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.1." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private UUID userId(String token) throws Exception {
        String body = mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private String createCategory(String token) throws Exception {
        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Estudo\",\"color\":\"#4C8BF5\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String createCourse(String token) throws Exception {
        String body = mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Curso\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String registerSession(String token) throws Exception {
        String categoryId = createCategory(token);
        String body = mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private UUID saveGithubRepository(UUID ownerId) {
        long githubRepoId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        return githubRepositoryRepository.saveAndFlush(new GitHubRepository(ownerId, githubRepoId, "dono", "repo",
                "dono/repo", null, null, "https://github.com/dono/repo", false, 0, 0, "main", null, null)).getId();
    }

    @Test
    void registraEListaSessao() throws Exception {
        String token = authToken();
        String categoryId = createCategory(token);
        String body = """
                {"categoryId":"%s","focusedMinutes":50,"startedAt":"2026-08-03T14:00:00-03:00","note":"estudo"}
                """.formatted(categoryId);

        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.focusedMinutes").value(50));

        mockMvc.perform(get("/sessions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listaDaMaisRecenteParaAMaisAntigaComPaginacao() throws Exception {
        String token = authToken();
        String categoryId = createCategory(token);
        for (String day : new String[] {"2026-08-01", "2026-08-03", "2026-08-02"}) {
            mockMvc.perform(post("/sessions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"categoryId":"%s","focusedMinutes":30,"startedAt":"%sT14:00:00-03:00"}
                                    """.formatted(categoryId, day)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/sessions").param("size", "2").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].startedAt").value(org.hamcrest.Matchers.startsWith("2026-08-03")))
                .andExpect(jsonPath("$[1].startedAt").value(org.hamcrest.Matchers.startsWith("2026-08-02")));

        mockMvc.perform(get("/sessions").param("size", "2").param("page", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].startedAt").value(org.hamcrest.Matchers.startsWith("2026-08-01")));
    }

    @Test
    void paginaNegativaETamanhoZeroSaoAjustados() throws Exception {
        String token = authToken();
        registerSession(token);

        mockMvc.perform(get("/sessions").param("page", "-3").param("size", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listaSoAsSessoesDoUsuario() throws Exception {
        String token = authToken();
        registerSession(authToken());

        mockMvc.perform(get("/sessions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejeitaMinutosNaoPositivos() throws Exception {
        String token = authToken();
        String categoryId = createCategory(token);
        String body = """
                {"categoryId":"%s","focusedMinutes":-5,"startedAt":"2026-08-03T14:00:00-03:00"}
                """.formatted(categoryId);

        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("focusedMinutes"));
    }

    @Test
    void categoriaInexistenteRetorna404() throws Exception {
        String token = authToken();
        String body = """
                {"categoryId":"11111111-1111-1111-1111-111111111111","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                """;

        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void categoriaDeOutroUsuarioRetorna404() throws Exception {
        String token = authToken();
        String otherCategoryId = createCategory(authToken());

        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                                """.formatted(otherCategoryId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void registraSessaoComCurso() throws Exception {
        String token = authToken();
        String categoryId = createCategory(token);
        String courseId = createCourse(token);

        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","courseId":"%s","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                                """.formatted(categoryId, courseId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(courseId));
    }

    @Test
    void cursoDeOutroUsuarioRetorna404() throws Exception {
        String token = authToken();
        String categoryId = createCategory(token);
        String otherCourseId = createCourse(authToken());

        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","courseId":"%s","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                                """.formatted(categoryId, otherCourseId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluiSessao() throws Exception {
        String token = authToken();
        String sessionId = registerSession(token);

        mockMvc.perform(delete("/sessions/{id}", sessionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/sessions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void excluirSessaoInexistenteRetorna404() throws Exception {
        String token = authToken();

        mockMvc.perform(delete("/sessions/{id}", UUID.randomUUID()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void sessaoDeOutroUsuarioRetorna404EmTodasAsRotas() throws Exception {
        String token = authToken();
        String otherToken = authToken();
        String otherSessionId = registerSession(otherToken);
        UUID repositoryId = saveGithubRepository(userId(token));

        mockMvc.perform(delete("/sessions/{id}", otherSessionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/sessions/{id}/github-activity", otherSessionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/sessions/{id}/github-repository", otherSessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryId\":\"" + repositoryId + "\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/sessions/{id}/github-repository", otherSessionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/sessions").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void vinculaEDesvinculaRepositorioDoGithub() throws Exception {
        String token = authToken();
        String sessionId = registerSession(token);
        UUID repositoryId = saveGithubRepository(userId(token));

        mockMvc.perform(put("/sessions/{id}/github-repository", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryId\":\"" + repositoryId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.githubRepositoryId").value(repositoryId.toString()));

        mockMvc.perform(get("/sessions").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].githubRepositoryId").value(repositoryId.toString()));

        mockMvc.perform(delete("/sessions/{id}/github-repository", sessionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.githubRepositoryId").value(nullValue()));

        mockMvc.perform(get("/sessions").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].githubRepositoryId").value(nullValue()));
    }

    @Test
    void vincularRepositorioDeOutroUsuarioRetorna404() throws Exception {
        String token = authToken();
        String sessionId = registerSession(token);
        UUID otherRepositoryId = saveGithubRepository(userId(authToken()));

        mockMvc.perform(put("/sessions/{id}/github-repository", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryId\":\"" + otherRepositoryId + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void vincularSemRepositorioRetorna400() throws Exception {
        String token = authToken();
        String sessionId = registerSession(token);

        mockMvc.perform(put("/sessions/{id}/github-repository", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atividadeDoGithubSemConexaoRetornaDesconectado() throws Exception {
        String token = authToken();
        String sessionId = registerSession(token);

        mockMvc.perform(get("/sessions/{id}/github-activity", sessionId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.commits", hasSize(0)));
    }

    @Test
    void categoriaComSessaoNaoPodeSerExcluida() throws Exception {
        String token = authToken();
        String categoryId = createCategory(token);
        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                                """.formatted(categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/categories/{id}", categoryId).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}
