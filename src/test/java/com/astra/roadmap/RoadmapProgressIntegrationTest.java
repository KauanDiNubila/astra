package com.astra.roadmap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
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
class RoadmapProgressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String newUserToken() throws Exception {
        String email = "roadmap-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mapper\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.6." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }

    private String postJson(String token, String path, String body) throws Exception {
        String response = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private String step(String token, String roadmapId, String title, int position, String parentId) throws Exception {
        String parent = parentId == null ? "null" : "\"" + parentId + "\"";
        return postJson(token, "/roadmaps/" + roadmapId + "/steps",
                "{\"title\":\"" + title + "\",\"position\":" + position + ",\"parentStepId\":" + parent + ",\"description\":null}");
    }

    private void mark(String token, String roadmapId, String stepId, String status) throws Exception {
        mockMvc.perform(patch("/roadmaps/" + roadmapId + "/steps/" + stepId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void roadmapListCarriesProgressAndMilestoneStates() throws Exception {
        String token = newUserToken();
        String roadmapId = postJson(token, "/roadmaps", "{\"title\":\"Backend\",\"source\":null}");
        String java = step(token, roadmapId, "Java", 1, null);
        String oop = step(token, roadmapId, "OOP", 1, java);
        String streams = step(token, roadmapId, "Streams", 2, java);
        String spring = step(token, roadmapId, "Spring", 2, null);
        String mvc = step(token, roadmapId, "MVC", 1, spring);
        step(token, roadmapId, "Security", 2, spring);
        String docker = step(token, roadmapId, "Docker", 3, null);
        String compose = step(token, roadmapId, "Compose", 1, docker);

        mark(token, roadmapId, oop, "DONE");
        mark(token, roadmapId, streams, "DONE");
        mark(token, roadmapId, mvc, "LEARNING");
        mark(token, roadmapId, compose, "SKIPPED");

        String body = mockMvc.perform(get("/roadmaps").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> roadmaps = JsonPath.read(body, "$[?(@.id == '" + roadmapId + "')]");
        assertThat(roadmaps).hasSize(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> progress = (Map<String, Object>) roadmaps.getFirst().get("progress");

        assertThat(progress.get("milestones")).isEqualTo(3);
        assertThat(progress.get("topics")).isEqualTo(5);
        assertThat(progress.get("done")).isEqualTo(2);
        assertThat(progress.get("total")).isEqualTo(7);
        assertThat(progress.get("started")).isEqualTo(true);
        assertThat(progress.get("milestoneStates")).isEqualTo(List.of("DONE", "ACTIVE", "TODO"));
    }

    @Test
    void newRoadmapStartsWithEmptyProgress() throws Exception {
        String token = newUserToken();
        String roadmapId = postJson(token, "/roadmaps", "{\"title\":\"Vazio\",\"source\":null}");
        String body = mockMvc.perform(get("/roadmaps").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> progress = JsonPath.read(body, "$[?(@.id == '" + roadmapId + "')].progress");
        assertThat(progress.getFirst().get("total")).isEqualTo(0);
        assertThat(progress.getFirst().get("started")).isEqualTo(false);
    }
}
