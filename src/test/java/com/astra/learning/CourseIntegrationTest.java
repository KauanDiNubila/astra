package com.astra.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private record TestUser(String token, UUID id) {
    }

    private TestUser newUser() throws Exception {
        String email = "learning-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Learner\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.7." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");
        String me = mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        return new TestUser(token, UUID.fromString(JsonPath.read(me, "$.id")));
    }

    private ResultActions call(TestUser user, MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("Authorization", "Bearer " + user.token());
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private String idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String course(TestUser user, Integer moduleCount) throws Exception {
        return idOf(call(user, post("/courses"),
                "{\"title\":\"Spring\",\"platform\":\"Udemy\",\"moduleCount\":" + moduleCount + "}")
                .andExpect(status().isCreated()));
    }

    private String module(TestUser user, String courseId, int position, Integer lessonCount) throws Exception {
        return idOf(call(user, post("/courses/{id}/modules", courseId),
                "{\"title\":\"Extra\",\"position\":" + position + ",\"lessonCount\":" + lessonCount + "}")
                .andExpect(status().isCreated()));
    }

    private String lesson(TestUser user, String courseId, String moduleId, String title, int position)
            throws Exception {
        return idOf(call(user, post("/courses/{c}/modules/{m}/lessons", courseId, moduleId),
                "{\"title\":\"" + title + "\",\"position\":" + position + "}")
                .andExpect(status().isCreated()));
    }

    private UUID githubRepository(UUID ownerId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into github_repository (id, user_id, github_repo_id, owner, name, full_name, primary_language,
                        html_url, private, star_count, fork_count, default_branch, recent_commit_count, synced_at)
                values (?, ?, ?, 'dono', 'repo', 'dono/repo', 'Java', 'https://github.com/dono/repo', false, 0, 0,
                        'main', 0, now())
                """, id, ownerId, ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
        return id;
    }

    @Test
    void criaCursoComModulosNumeradosEPlanejado() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 3);

        call(user, get("/courses/{id}", courseId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring"))
                .andExpect(jsonPath("$.platform").value("Udemy"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.progress").value(0.0))
                .andExpect(jsonPath("$.modules.length()").value(3))
                .andExpect(jsonPath("$.modules[0].title").value("Módulo 1"))
                .andExpect(jsonPath("$.modules[2].title").value("Módulo 3"))
                .andExpect(jsonPath("$.modules[2].position").value(3))
                .andExpect(jsonPath("$.modules[0].completed").value(false));
    }

    @Test
    void criarSemModulosERespostaDoCadastro() throws Exception {
        TestUser user = newUser();

        call(user, post("/courses"), "{\"title\":\"Docker\",\"platform\":null,\"moduleCount\":null}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Docker"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.totalLessons").value(0))
                .andExpect(jsonPath("$.lastStudiedAt").doesNotExist());
    }

    @Test
    void moduloNovoCriaAulasNumeradas() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);

        call(user, post("/courses/{id}/modules", courseId), "{\"title\":\"Básico\",\"position\":1,\"lessonCount\":2}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Básico"))
                .andExpect(jsonPath("$.totalLessons").value(2))
                .andExpect(jsonPath("$.lessons[0].title").value("Aula 1"))
                .andExpect(jsonPath("$.lessons[1].title").value("Aula 2"))
                .andExpect(jsonPath("$.lessons[1].position").value(2))
                .andExpect(jsonPath("$.lessons[1].completed").value(false));
    }

    @Test
    void concluirAulasAtualizaProgressoNoDetalheENaLista() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 0);
        String first = lesson(user, courseId, moduleId, "Intro", 1);
        lesson(user, courseId, moduleId, "Beans", 2);
        lesson(user, courseId, moduleId, "JPA", 3);
        lesson(user, courseId, moduleId, "Testes", 4);

        call(user, patch("/courses/{c}/modules/{m}/lessons/{l}", courseId, moduleId, first), "{\"completed\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Intro"))
                .andExpect(jsonPath("$.completed").value(true));

        call(user, get("/courses/{id}", courseId), null)
                .andExpect(jsonPath("$.totalLessons").value(4))
                .andExpect(jsonPath("$.completedLessons").value(1))
                .andExpect(jsonPath("$.progress").value(0.25))
                .andExpect(jsonPath("$.modules[0].completedLessons").value(1))
                .andExpect(jsonPath("$.modules[0].lessons[0].completed").value(true));

        call(user, get("/courses"), null)
                .andExpect(jsonPath("$[?(@.id == '" + courseId + "')].totalLessons").value(4))
                .andExpect(jsonPath("$[?(@.id == '" + courseId + "')].completedLessons").value(1))
                .andExpect(jsonPath("$[?(@.id == '" + courseId + "')].progress").value(0.25));
    }

    @Test
    void moduloConcluidoQuandoTodasAsAulasEstaoFeitas() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 0);
        String only = lesson(user, courseId, moduleId, "Única", 1);

        call(user, patch("/courses/{c}/modules/{m}/lessons/{l}", courseId, moduleId, only), "{\"completed\":true}")
                .andExpect(status().isOk());

        call(user, get("/courses/{id}", courseId), null)
                .andExpect(jsonPath("$.modules[0].completed").value(true));
    }

    @Test
    void novaAulaReabreModuloMarcadoComoConcluido() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 1);
        String moduleId = JsonPath.read(call(user, get("/courses/{id}", courseId), null)
                .andReturn().getResponse().getContentAsString(), "$.modules[0].id");

        call(user, patch("/courses/{c}/modules/{m}", courseId, moduleId), "{\"completed\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        lesson(user, courseId, moduleId, "Nova", 1);

        call(user, get("/courses/{id}", courseId), null)
                .andExpect(jsonPath("$.modules[0].completed").value(false))
                .andExpect(jsonPath("$.modules[0].totalLessons").value(1));
    }

    @Test
    void progressoDoModuloMarcaAteAPosicaoEDesmarcaOResto() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 4);

        call(user, patch("/courses/{c}/modules/{m}/progress", courseId, moduleId), "{\"uptoPosition\":2}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLessons").value(2))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.lessons[1].completed").value(true))
                .andExpect(jsonPath("$.lessons[2].completed").value(false));

        call(user, patch("/courses/{c}/modules/{m}/progress", courseId, moduleId), "{\"uptoPosition\":4}")
                .andExpect(jsonPath("$.completedLessons").value(4))
                .andExpect(jsonPath("$.completed").value(true));

        call(user, patch("/courses/{c}/modules/{m}/progress", courseId, moduleId), "{\"uptoPosition\":0}")
                .andExpect(jsonPath("$.completedLessons").value(0));

        call(user, get("/courses/{id}", courseId), null)
                .andExpect(jsonPath("$.completedLessons").value(0));
    }

    @Test
    void apagarAulaRenumeraAsQueSobram() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 0);
        lesson(user, courseId, moduleId, "A", 1);
        String second = lesson(user, courseId, moduleId, "B", 2);
        lesson(user, courseId, moduleId, "C", 3);

        call(user, delete("/courses/{c}/modules/{m}/lessons/{l}", courseId, moduleId, second), null)
                .andExpect(status().isNoContent());

        call(user, get("/courses/{id}", courseId), null)
                .andExpect(jsonPath("$.modules[0].lessons.length()").value(2))
                .andExpect(jsonPath("$.modules[0].lessons[0].title").value("A"))
                .andExpect(jsonPath("$.modules[0].lessons[0].position").value(1))
                .andExpect(jsonPath("$.modules[0].lessons[1].title").value("C"))
                .andExpect(jsonPath("$.modules[0].lessons[1].position").value(2));
    }

    @Test
    void editarModuloAparaTituloERecusaEmBranco() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 1);

        call(user, patch("/courses/{c}/modules/{m}", courseId, moduleId), "{\"title\":\"  Avançado  \"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Avançado"))
                .andExpect(jsonPath("$.totalLessons").value(1))
                .andExpect(jsonPath("$.completed").value(false));

        call(user, patch("/courses/{c}/modules/{m}", courseId, moduleId), "{\"title\":\"   \"}")
                .andExpect(status().isConflict());

        call(user, patch("/courses/{c}/modules/{m}", courseId, moduleId), "{}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Avançado"));
    }

    @Test
    void cursoDeOutraPessoaRetorna404EmTudo() throws Exception {
        TestUser owner = newUser();
        TestUser other = newUser();
        String courseId = course(owner, 0);
        String moduleId = module(owner, courseId, 1, 0);
        String lessonId = lesson(owner, courseId, moduleId, "A", 1);

        call(other, get("/courses/{id}", courseId), null).andExpect(status().isNotFound());
        call(other, post("/courses/{id}/modules", courseId), "{\"title\":\"X\",\"position\":2,\"lessonCount\":0}")
                .andExpect(status().isNotFound());
        call(other, post("/courses/{c}/modules/{m}/lessons", courseId, moduleId), "{\"title\":\"X\",\"position\":2}")
                .andExpect(status().isNotFound());
        call(other, patch("/courses/{c}/modules/{m}/lessons/{l}", courseId, moduleId, lessonId), "{\"completed\":true}")
                .andExpect(status().isNotFound());
        call(other, delete("/courses/{c}/modules/{m}/lessons/{l}", courseId, moduleId, lessonId), null)
                .andExpect(status().isNotFound());
        call(other, patch("/courses/{c}/modules/{m}", courseId, moduleId), "{\"completed\":true}")
                .andExpect(status().isNotFound());
        call(other, patch("/courses/{c}/modules/{m}/progress", courseId, moduleId), "{\"uptoPosition\":1}")
                .andExpect(status().isNotFound());
        call(other, get("/courses/{id}/github-repositories", courseId), null).andExpect(status().isNotFound());
        call(other, get("/courses"), null)
                .andExpect(jsonPath("$[?(@.id == '" + courseId + "')]").isEmpty());

        call(owner, get("/courses/{id}", courseId), null)
                .andExpect(jsonPath("$.completedLessons").value(0))
                .andExpect(jsonPath("$.modules[0].lessons.length()").value(1));
    }

    @Test
    void moduloOuAulaDeOutroLugarRetorna404() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String otherCourseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 0);
        String otherModuleId = module(user, courseId, 2, 0);
        String lessonId = lesson(user, courseId, moduleId, "A", 1);

        call(user, post("/courses/{c}/modules/{m}/lessons", otherCourseId, moduleId), "{\"title\":\"X\",\"position\":1}")
                .andExpect(status().isNotFound());
        call(user, patch("/courses/{c}/modules/{m}/lessons/{l}", courseId, otherModuleId, lessonId),
                "{\"completed\":true}")
                .andExpect(status().isNotFound());
        call(user, delete("/courses/{c}/modules/{m}/lessons/{l}", courseId, otherModuleId, lessonId), null)
                .andExpect(status().isNotFound());
        call(user, patch("/courses/{c}/modules/{m}", otherCourseId, moduleId), "{\"completed\":true}")
                .andExpect(status().isNotFound());
        call(user, get("/courses/{id}", UUID.randomUUID()), null).andExpect(status().isNotFound());
    }

    @Test
    void validaOsDadosDeEntrada() throws Exception {
        TestUser user = newUser();
        String courseId = course(user, 0);
        String moduleId = module(user, courseId, 1, 0);

        call(user, post("/courses"), "{\"title\":\" \"}").andExpect(status().isBadRequest());
        call(user, post("/courses"), "{\"title\":\"" + "a".repeat(161) + "\"}").andExpect(status().isBadRequest());
        call(user, post("/courses"), "{\"title\":\"X\",\"moduleCount\":-1}").andExpect(status().isBadRequest());
        call(user, post("/courses/{id}/modules", courseId), "{\"title\":\"X\"}").andExpect(status().isBadRequest());
        call(user, post("/courses/{c}/modules/{m}/lessons", courseId, moduleId), "{\"title\":\"X\",\"position\":-1}")
                .andExpect(status().isBadRequest());
        call(user, patch("/courses/{c}/modules/{m}/progress", courseId, moduleId), "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void vinculaEDesvinculaRepositorioDoGithub() throws Exception {
        TestUser user = newUser();
        TestUser other = newUser();
        String courseId = course(user, 0);
        UUID repositoryId = githubRepository(user.id());
        UUID othersRepository = githubRepository(other.id());

        call(user, post("/courses/{id}/github-repositories", courseId), "{\"repositoryId\":\"" + repositoryId + "\"}")
                .andExpect(status().isNoContent());
        call(user, post("/courses/{id}/github-repositories", courseId), "{\"repositoryId\":\"" + repositoryId + "\"}")
                .andExpect(status().isNoContent());
        call(user, post("/courses/{id}/github-repositories", courseId),
                "{\"repositoryId\":\"" + othersRepository + "\"}")
                .andExpect(status().isNotFound());

        call(user, get("/courses/{id}/github-repositories", courseId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories.length()").value(1))
                .andExpect(jsonPath("$.repositories[0].fullName").value("dono/repo"))
                .andExpect(jsonPath("$.totalRecentCommits").value(0));

        call(user, delete("/courses/{id}/github-repositories/{r}", courseId, repositoryId), null)
                .andExpect(status().isNoContent());
        call(user, get("/courses/{id}/github-repositories", courseId), null)
                .andExpect(jsonPath("$.repositories.length()").value(0));
    }

    @Test
    void metaUmaPorTipoQueAtualizaAoDefinirDeNovo() throws Exception {
        TestUser user = newUser();

        call(user, put("/goals"), "{\"type\":\"DAILY\",\"targetHours\":2}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DAILY"))
                .andExpect(jsonPath("$.targetHours").value(2));
        call(user, put("/goals"), "{\"type\":\"DAILY\",\"targetHours\":3}").andExpect(status().isOk());
        call(user, put("/goals"), "{\"type\":\"WEEKLY\",\"targetHours\":10}").andExpect(status().isOk());

        String body = call(user, get("/goals"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.type == 'DAILY')].targetHours").value(3))
                .andExpect(jsonPath("$[?(@.type == 'WEEKLY')].targetHours").value(10))
                .andReturn().getResponse().getContentAsString();
        List<String> types = JsonPath.read(body, "$[*].type");
        assertThat(types).containsExactlyInAnyOrder("DAILY", "WEEKLY");

        call(newUser(), get("/goals"), null).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void metaInvalidaRetorna400() throws Exception {
        TestUser user = newUser();

        call(user, put("/goals"), "{\"type\":\"DAILY\",\"targetHours\":0}").andExpect(status().isBadRequest());
        call(user, put("/goals"), "{\"type\":\"YEARLY\",\"targetHours\":1}").andExpect(status().isBadRequest());
        call(user, put("/goals"), "{\"targetHours\":1}").andExpect(status().isBadRequest());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/courses")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/goals")).andExpect(status().isUnauthorized());
    }
}
