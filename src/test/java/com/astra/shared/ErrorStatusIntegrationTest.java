package com.astra.shared;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ErrorStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String newUserToken() throws Exception {
        String email = "errors-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Errors\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
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
        return JsonPath.read(login, "$.accessToken");
    }

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe").header("Authorization", "Bearer " + newUserToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"));
    }

    @Test
    void parametroComValorInvalidoRetorna400() throws Exception {
        mockMvc.perform(get("/ranking").param("period", "WEEK").header("Authorization", "Bearer " + newUserToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisição inválida"));
    }

    @Test
    void corpoMalFormadoRetorna400() throws Exception {
        mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + newUserToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nao e json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void metodoNaoSuportadoRetorna405() throws Exception {
        mockMvc.perform(delete("/dashboard").header("Authorization", "Bearer " + newUserToken()))
                .andExpect(status().isMethodNotAllowed());
    }
}
