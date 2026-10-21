package com.astra.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
class CourseLastStudiedIntegrationTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private MockMvc mockMvc;

    private String newUserToken() throws Exception {
        String email = "course-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Learner\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.5." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(login, "$.accessToken");
    }

    private String create(String token, String path, String body) throws Exception {
        String created = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private void session(String token, String categoryId, String courseId, int daysAgo) throws Exception {
        String startedAt = LocalDate.now(ZONE).minusDays(daysAgo).atTime(12, 0).atZone(ZONE).toOffsetDateTime().toString();
        create(token, "/sessions", "{\"categoryId\":\"" + categoryId + "\",\"courseId\":\"" + courseId
                + "\",\"focusedMinutes\":25,\"startedAt\":\"" + startedAt + "\",\"note\":null}");
    }

    @Test
    void courseListReportsWhenEachCourseWasLastStudied() throws Exception {
        String token = newUserToken();
        String categoryId = create(token, "/categories", "{\"name\":\"Estudos\"}");
        String studied = create(token, "/courses", "{\"title\":\"Spring\",\"platform\":null,\"moduleCount\":null}");
        String untouched = create(token, "/courses", "{\"title\":\"Docker\",\"platform\":null,\"moduleCount\":null}");

        session(token, categoryId, studied, 5);
        session(token, categoryId, studied, 1);

        String body = mockMvc.perform(get("/courses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> courses = JsonPath.read(body, "$");
        Map<String, Object> studiedCourse = courses.stream().filter(c -> studied.equals(c.get("id"))).findFirst().orElseThrow();
        Map<String, Object> untouchedCourse = courses.stream().filter(c -> untouched.equals(c.get("id"))).findFirst().orElseThrow();

        OffsetDateTime expected = LocalDate.now(ZONE).minusDays(1).atTime(12, 0).atZone(ZONE).toOffsetDateTime();
        assertThat(OffsetDateTime.parse((String) studiedCourse.get("lastStudiedAt")).toInstant()).isEqualTo(expected.toInstant());
        assertThat(untouchedCourse.get("lastStudiedAt")).isNull();
    }
}
