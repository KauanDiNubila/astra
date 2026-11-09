package com.astra.tracking.category;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    static String authToken(MockMvc mockMvc) throws Exception {
        String email = "cat-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String body = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.3." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private ResultActions create(String token, String json) throws Exception {
        return mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String createCategory(String token, String name) throws Exception {
        String body = create(token, "{\"name\":\"" + name + "\",\"color\":\"#4C8BF5\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void criaEListaCategoria() throws Exception {
        String token = authToken(mockMvc);

        create(token, "{\"name\":\"Leitura\",\"color\":\"#4C8BF5\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Leitura"))
                .andExpect(jsonPath("$.color").value("#4C8BF5"));

        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Leitura"));
    }

    @Test
    void corEOpcional() throws Exception {
        String token = authToken(mockMvc);

        create(token, "{\"name\":\"Sem cor\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.color").doesNotExist());
    }

    @Test
    void listaSoAsCategoriasDoUsuario() throws Exception {
        String token = authToken(mockMvc);
        createCategory(authToken(mockMvc), "De outro");

        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void validaNomeECor() throws Exception {
        String token = authToken(mockMvc);

        create(token, "{\"name\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
        create(token, "{\"name\":\"" + "a".repeat(81) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
        create(token, "{\"name\":\"Ok\",\"color\":\"" + "#".repeat(21) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("color"));
    }

    @Test
    void mesmoNomeEmUsuariosDiferentesEPermitido() throws Exception {
        createCategory(authToken(mockMvc), "Estudo");
        createCategory(authToken(mockMvc), "Estudo");
    }

    @Test
    void excluiCategoria() throws Exception {
        String token = authToken(mockMvc);
        String id = createCategory(token, "Temporaria");

        mockMvc.perform(delete("/categories/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void excluirCategoriaDeOutroUsuarioOuInexistenteRetorna404() throws Exception {
        String token = authToken(mockMvc);
        String otherToken = authToken(mockMvc);
        String otherId = createCategory(otherToken, "Alheia");

        mockMvc.perform(delete("/categories/{id}", otherId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/categories/{id}", UUID.randomUUID()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + otherToken))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void categoriaComSessaoNaoPodeSerExcluida() throws Exception {
        String token = authToken(mockMvc);
        String id = createCategory(token, "Usada");
        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + id
                                + "\",\"focusedMinutes\":30,\"startedAt\":\"2026-08-03T14:00:00-03:00\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/categories/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Usada")));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/categories")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
                .andExpect(status().isUnauthorized());
    }
}
