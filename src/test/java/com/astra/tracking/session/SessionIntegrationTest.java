package com.astra.tracking.session;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class SessionIntegrationTest {

    private static final String SEED_CATEGORY_ID = "00000000-0000-0000-0000-000000000002";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registraEListaSessao() throws Exception {
        String body = """
                {"categoryId":"%s","focusedMinutes":50,"startedAt":"2026-08-03T14:00:00-03:00","note":"estudo"}
                """.formatted(SEED_CATEGORY_ID);

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.categoryId").value(SEED_CATEGORY_ID))
                .andExpect(jsonPath("$.focusedMinutes").value(50))
                .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].focusedMinutes").value(50));
    }

    @Test
    void rejeitaMinutosNaoPositivos() throws Exception {
        String body = """
                {"categoryId":"%s","focusedMinutes":-5,"startedAt":"2026-08-03T14:00:00-03:00"}
                """.formatted(SEED_CATEGORY_ID);

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("focusedMinutes"));
    }

    @Test
    void retorna404ParaCategoriaInexistente() throws Exception {
        String body = """
                {"categoryId":"11111111-1111-1111-1111-111111111111","focusedMinutes":30,"startedAt":"2026-08-03T14:00:00-03:00"}
                """;

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
