package com.astra.roadmap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class RoadmapIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private void newRequest() {
        entityManager.flush();
        entityManager.clear();
    }

    private String newUser() throws Exception {
        String email = "roadmap-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mapper\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.8." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }

    private ResultActions call(String token, MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private String idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String roadmap(String token) throws Exception {
        return idOf(call(token, post("/roadmaps"), "{\"title\":\"Meu\",\"source\":\"livro\"}")
                .andExpect(status().isCreated()));
    }

    private String step(String token, String roadmapId, String title, int position, String parentId)
            throws Exception {
        String parent = parentId == null ? "null" : "\"" + parentId + "\"";
        return idOf(call(token, post("/roadmaps/{id}/steps", roadmapId),
                "{\"title\":\"" + title + "\",\"position\":" + position + ",\"parentStepId\":" + parent
                        + ",\"description\":\"sobre " + title + "\"}")
                .andExpect(status().isCreated()));
    }

    private String predefinedRoadmap(String token) throws Exception {
        String body = call(token, get("/roadmaps"), null).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(body, "$[?(@.predefined == true)].id");
        return ids.getFirst();
    }

    private String firstStepOf(String token, String roadmapId) throws Exception {
        return JsonPath.read(call(token, get("/roadmaps/{id}", roadmapId), null)
                .andReturn().getResponse().getContentAsString(), "$.steps[0].id");
    }

    private String course(String token) throws Exception {
        return idOf(call(token, post("/courses"), "{\"title\":\"Spring\",\"platform\":null,\"moduleCount\":1}")
                .andExpect(status().isCreated()));
    }

    private ResultActions mark(String token, String roadmapId, String stepId, String status) throws Exception {
        String value = status == null ? "null" : "\"" + status + "\"";
        return call(token, patch("/roadmaps/{r}/steps/{s}", roadmapId, stepId), "{\"status\":" + value + "}");
    }

    @Test
    void criaRoadmapProprioEMostraPassosComPaiEDescricao() throws Exception {
        String token = newUser();

        call(token, post("/roadmaps"), "{\"title\":\"Meu\",\"source\":\"livro\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Meu"))
                .andExpect(jsonPath("$.source").value("livro"))
                .andExpect(jsonPath("$.predefined").value(false))
                .andExpect(jsonPath("$.progress.total").value(0))
                .andExpect(jsonPath("$.progress.milestoneStates.length()").value(0));

        String roadmapId = roadmap(token);
        String parent = step(token, roadmapId, "Java", 1, null);
        call(token, post("/roadmaps/{id}/steps", roadmapId),
                "{\"title\":\"OOP\",\"position\":1,\"parentStepId\":\"" + parent + "\",\"description\":null}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("OOP"))
                .andExpect(jsonPath("$.parentStepId").value(parent))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.resources.length()").value(0));

        call(token, get("/roadmaps/{id}", roadmapId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predefined").value(false))
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.steps[?(@.id == '" + parent + "')].description").value("sobre Java"));
    }

    @Test
    void listaTemOsProntosEOsMeusMasNaoOsDosOutros() throws Exception {
        String token = newUser();
        String other = newUser();
        String mine = roadmap(token);
        String theirs = roadmap(other);

        String body = call(token, get("/roadmaps"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + mine + "')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.id == '" + theirs + "')]").isEmpty())
                .andReturn().getResponse().getContentAsString();
        List<Boolean> predefined = JsonPath.read(body, "$[?(@.predefined == true)].predefined");
        org.assertj.core.api.Assertions.assertThat(predefined).isNotEmpty();

        call(token, get("/roadmaps/{id}", theirs), null).andExpect(status().isNotFound());
        call(token, get("/roadmaps/{id}", UUID.randomUUID()), null).andExpect(status().isNotFound());
    }

    @Test
    void roadmapProntoAceitaStatusPorPessoaMasNaoPassoNovo() throws Exception {
        String token = newUser();
        String other = newUser();
        String predefined = predefinedRoadmap(token);
        String stepId = firstStepOf(token, predefined);

        call(token, get("/roadmaps/{id}", predefined), null).andExpect(jsonPath("$.predefined").value(true));
        call(token, post("/roadmaps/{id}/steps", predefined), "{\"title\":\"X\",\"position\":1}")
                .andExpect(status().isNotFound());

        mark(token, predefined, stepId, "DONE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stepId))
                .andExpect(jsonPath("$.status").value("DONE"));

        call(token, get("/roadmaps/{id}", predefined), null)
                .andExpect(jsonPath("$.steps[?(@.id == '" + stepId + "')].status").value("DONE"));
        call(other, get("/roadmaps/{id}", predefined), null)
                .andExpect(jsonPath("$.steps[?(@.id == '" + stepId + "' && @.status == 'DONE')]").isEmpty());
    }

    @Test
    void statusTrocaELimpaComNulo() throws Exception {
        String token = newUser();
        String roadmapId = roadmap(token);
        String stepId = step(token, roadmapId, "Java", 1, null);

        mark(token, roadmapId, stepId, "LEARNING").andExpect(jsonPath("$.status").value("LEARNING"));
        newRequest();
        mark(token, roadmapId, stepId, "SKIPPED").andExpect(jsonPath("$.status").value("SKIPPED"));
        newRequest();
        mark(token, roadmapId, stepId, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").doesNotExist());

        call(token, get("/roadmaps/{id}", roadmapId), null)
                .andExpect(jsonPath("$.steps[0].status").doesNotExist());
        mark(token, roadmapId, stepId, "FINISHED").andExpect(status().isBadRequest());
    }

    @Test
    void passoDeOutroRoadmapOuDeOutraPessoaRetorna404() throws Exception {
        String token = newUser();
        String other = newUser();
        String roadmapId = roadmap(token);
        String otherRoadmap = roadmap(token);
        String foreignStep = step(token, otherRoadmap, "Fora", 1, null);
        String theirs = roadmap(other);
        String theirStep = step(other, theirs, "Deles", 1, null);

        call(token, post("/roadmaps/{id}/steps", roadmapId),
                "{\"title\":\"X\",\"position\":1,\"parentStepId\":\"" + foreignStep + "\"}")
                .andExpect(status().isNotFound());
        call(token, post("/roadmaps/{id}/steps", roadmapId),
                "{\"title\":\"X\",\"position\":1,\"parentStepId\":\"" + UUID.randomUUID() + "\"}")
                .andExpect(status().isNotFound());
        mark(token, roadmapId, foreignStep, "DONE").andExpect(status().isNotFound());
        mark(token, theirs, theirStep, "DONE").andExpect(status().isNotFound());
        call(token, post("/roadmaps/{id}/steps", theirs), "{\"title\":\"X\",\"position\":1}")
                .andExpect(status().isNotFound());
    }

    @Test
    void validaRoadmapEPasso() throws Exception {
        String token = newUser();
        String roadmapId = roadmap(token);

        call(token, post("/roadmaps"), "{\"title\":\" \"}").andExpect(status().isBadRequest());
        call(token, post("/roadmaps"), "{\"title\":\"X\",\"source\":\"" + "s".repeat(201) + "\"}")
                .andExpect(status().isBadRequest());
        call(token, post("/roadmaps/{id}/steps", roadmapId), "{\"title\":\"X\"}").andExpect(status().isBadRequest());
        call(token, post("/roadmaps/{id}/steps", roadmapId),
                "{\"title\":\"X\",\"position\":1,\"description\":\"" + "d".repeat(2001) + "\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void linksDoPassoEntramEmOrdemESaem() throws Exception {
        String token = newUser();
        String roadmapId = roadmap(token);
        String stepId = step(token, roadmapId, "Java", 1, null);

        call(token, post("/steps/{id}/resources", stepId), "{\"label\":\"Docs\",\"url\":\"https://docs\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Docs"))
                .andExpect(jsonPath("$.position").value(0));
        String second = idOf(call(token, post("/steps/{id}/resources", stepId),
                "{\"label\":\"Vídeo\",\"url\":\"https://video\"}")
                .andExpect(jsonPath("$.position").value(1)));

        call(token, get("/steps/{id}/resources", stepId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].label").value("Docs"))
                .andExpect(jsonPath("$[1].url").value("https://video"));
        call(token, get("/roadmaps/{id}", roadmapId), null)
                .andExpect(jsonPath("$.steps[0].resources.length()").value(2));
        mark(token, roadmapId, stepId, "DONE").andExpect(jsonPath("$.resources.length()").value(2));

        call(token, delete("/steps/{s}/resources/{r}", stepId, second), null).andExpect(status().isNoContent());
        call(token, get("/steps/{id}/resources", stepId), null).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void linksSoNoPassoProprioMasLeituraNoPronto() throws Exception {
        String token = newUser();
        String other = newUser();
        String predefinedStep = firstStepOf(token, predefinedRoadmap(token));
        String theirStep = step(other, roadmap(other), "Deles", 1, null);
        String mine = step(token, roadmap(token), "Meu", 1, null);

        call(token, get("/steps/{id}/resources", predefinedStep), null).andExpect(status().isOk());
        call(token, post("/steps/{id}/resources", predefinedStep), "{\"label\":\"X\",\"url\":\"https://x\"}")
                .andExpect(status().isNotFound());
        call(token, get("/steps/{id}/resources", theirStep), null).andExpect(status().isNotFound());
        call(token, post("/steps/{id}/resources", theirStep), "{\"label\":\"X\",\"url\":\"https://x\"}")
                .andExpect(status().isNotFound());
        call(token, delete("/steps/{s}/resources/{r}", theirStep, UUID.randomUUID()), null)
                .andExpect(status().isNotFound());
        call(token, get("/steps/{id}/resources", UUID.randomUUID()), null).andExpect(status().isNotFound());
        call(token, post("/steps/{id}/resources", mine), "{\"label\":\" \",\"url\":\"https://x\"}")
                .andExpect(status().isBadRequest());
        call(token, post("/steps/{id}/resources", mine), "{\"label\":\"X\",\"url\":\"" + "u".repeat(501) + "\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void pinLigaCursoAoPassoEAtualizaSeRepetir() throws Exception {
        String token = newUser();
        String other = newUser();
        String predefined = predefinedRoadmap(token);
        String stepId = firstStepOf(token, predefined);
        String courseId = course(token);

        String pinId = idOf(call(token, post("/steps/{id}/pins", stepId),
                "{\"courseId\":\"" + courseId + "\",\"status\":\"usando\",\"rating\":4}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.stepId").value(stepId))
                .andExpect(jsonPath("$.status").value("usando"))
                .andExpect(jsonPath("$.rating").value(4)));
        call(token, post("/steps/{id}/pins", stepId), "{\"courseId\":\"" + courseId + "\",\"rating\":5}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(pinId))
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.rating").value(5));

        call(token, get("/roadmaps/{id}/pins", predefined), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + stepId + "'].length()").value(1))
                .andExpect(jsonPath("$['" + stepId + "'][0].id").value(pinId));
        call(other, get("/roadmaps/{id}/pins", predefined), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + stepId + "']").doesNotExist());

        call(token, delete("/steps/{s}/pins/{p}", stepId, pinId), null).andExpect(status().isNoContent());
        call(token, get("/roadmaps/{id}/pins", predefined), null)
                .andExpect(jsonPath("$['" + stepId + "']").doesNotExist());
    }

    @Test
    void pinRecusaCursoAlheioPassoErradoENotaForaDaFaixa() throws Exception {
        String token = newUser();
        String other = newUser();
        String roadmapId = roadmap(token);
        String stepId = step(token, roadmapId, "Java", 1, null);
        String otherStep = step(token, roadmapId, "Spring", 2, null);
        String courseId = course(token);
        String theirCourse = course(other);
        String theirStep = step(other, roadmap(other), "Deles", 1, null);

        call(token, post("/steps/{id}/pins", stepId), "{\"courseId\":\"" + theirCourse + "\"}")
                .andExpect(status().isNotFound());
        call(token, post("/steps/{id}/pins", theirStep), "{\"courseId\":\"" + courseId + "\"}")
                .andExpect(status().isNotFound());
        call(token, post("/steps/{id}/pins", stepId), "{\"courseId\":\"" + courseId + "\",\"rating\":6}")
                .andExpect(status().isBadRequest());
        call(token, post("/steps/{id}/pins", stepId), "{\"rating\":3}").andExpect(status().isBadRequest());

        String pinId = idOf(call(token, post("/steps/{id}/pins", stepId), "{\"courseId\":\"" + courseId + "\"}")
                .andExpect(status().isCreated()));
        call(token, delete("/steps/{s}/pins/{p}", otherStep, pinId), null).andExpect(status().isNotFound());
        call(other, delete("/steps/{s}/pins/{p}", stepId, pinId), null).andExpect(status().isNotFound());
        call(token, delete("/steps/{s}/pins/{p}", stepId, UUID.randomUUID()), null).andExpect(status().isNotFound());
        call(token, get("/roadmaps/{id}/pins", roadmapId), null)
                .andExpect(jsonPath("$['" + stepId + "'].length()").value(1));
        call(other, get("/roadmaps/{id}/pins", roadmapId), null).andExpect(status().isNotFound());
    }

    @Test
    void evidenciaDoGithubSemConexaoEPassoAlheio() throws Exception {
        String token = newUser();
        String other = newUser();
        String stepId = step(token, roadmap(token), "Java", 1, null);
        String theirStep = step(other, roadmap(other), "Deles", 1, null);

        call(token, get("/steps/{id}/github-evidence", stepId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.matchedRepositories.length()").value(0));
        call(token, get("/steps/{id}/github-evidence", theirStep), null).andExpect(status().isNotFound());
    }

    @Test
    void exportacaoTrazCursosMetasRoadmapsProgressoEPins() throws Exception {
        String token = newUser();
        String other = newUser();
        String courseId = course(token);
        String moduleId = JsonPath.read(call(token, get("/courses/{id}", courseId), null)
                .andReturn().getResponse().getContentAsString(), "$.modules[0].id");
        call(token, post("/courses/{c}/modules/{m}/lessons", courseId, moduleId), "{\"title\":\"Intro\",\"position\":1}")
                .andExpect(status().isCreated());
        call(token, put("/goals"), "{\"type\":\"DAILY\",\"targetHours\":2}").andExpect(status().isOk());
        String roadmapId = roadmap(token);
        String stepId = step(token, roadmapId, "Java", 1, null);
        mark(token, roadmapId, stepId, "DONE").andExpect(status().isOk());
        call(token, post("/steps/{id}/pins", stepId), "{\"courseId\":\"" + courseId + "\",\"rating\":3}")
                .andExpect(status().isCreated());
        roadmap(other);

        call(token, get("/me/export"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.courses[0].title").value("Spring"))
                .andExpect(jsonPath("$.courses[0].status").value("PLANNED"))
                .andExpect(jsonPath("$.courses[0].modules[0].title").value("Módulo 1"))
                .andExpect(jsonPath("$.courses[0].modules[0].lessons[0].title").value("Intro"))
                .andExpect(jsonPath("$.courses[0].githubRepositoryIds.length()").value(0))
                .andExpect(jsonPath("$.goals[0].type").value("DAILY"))
                .andExpect(jsonPath("$.goals[0].targetHours").value(2))
                .andExpect(jsonPath("$.roadmaps.ownRoadmaps.length()").value(1))
                .andExpect(jsonPath("$.roadmaps.ownRoadmaps[0].source").value("livro"))
                .andExpect(jsonPath("$.roadmaps.ownRoadmaps[0].steps[0].title").value("Java"))
                .andExpect(jsonPath("$.roadmaps.ownRoadmaps[0].steps[0].description").value("sobre Java"))
                .andExpect(jsonPath("$.roadmaps.stepProgress.length()").value(1))
                .andExpect(jsonPath("$.roadmaps.stepProgress[0].status").value("DONE"))
                .andExpect(jsonPath("$.roadmaps.stepProgress[0].completedAt").exists())
                .andExpect(jsonPath("$.coursePins.length()").value(1))
                .andExpect(jsonPath("$.coursePins[0].stepId").value(stepId))
                .andExpect(jsonPath("$.coursePins[0].rating").value(3));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/roadmaps")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/steps/{id}/resources", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }
}
