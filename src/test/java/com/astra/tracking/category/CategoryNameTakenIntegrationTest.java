package com.astra.tracking.category;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CategoryNameTakenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String authToken() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.4." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private ResultActions create(String token, String name) throws Exception {
        return mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"));
    }

    @Test
    void nomeRepetidoDoMesmoUsuarioRetorna409() throws Exception {
        String token = authToken();
        create(token, "Leitura").andExpect(status().isCreated());

        create(token, "Leitura")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category name already in use: Leitura"));
    }

    @Test
    void mesmoNomeEmUsuariosDiferentesContinuaPermitido() throws Exception {
        create(authToken(), "Leitura").andExpect(status().isCreated());
        create(authToken(), "Leitura").andExpect(status().isCreated());
    }
}
