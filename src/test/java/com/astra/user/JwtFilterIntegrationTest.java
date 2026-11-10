package com.astra.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.shared.crypto.JwtService;
import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.util.UUID;
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
class JwtFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private UUID register() throws Exception {
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jwt\",\"email\":\"jwt-" + UUID.randomUUID()
                                + "@astra.local\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    @Test
    void tokenDeSessaoValidoAutentica() throws Exception {
        UUID userId = register();

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + jwtService.generateToken(userId)))
                .andExpect(status().isOk());
    }

    @Test
    void tokenInvalidoOuDeOutroFormatoNaoAutentica() throws Exception {
        mockMvc.perform(get("/me").header("Authorization", "Bearer nao-e-um-jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me").header("Authorization", "Basic abc"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me").header("Authorization", "Bearer " + jwtService.generateToken(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDePropositoUnicoNaoServeComoSessao() throws Exception {
        UUID userId = register();
        String purposeToken = jwtService.generatePurposeToken(userId, "github-connect", Duration.ofMinutes(5));

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + purposeToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComumNaoEntraNaAdministracao() throws Exception {
        UUID userId = register();

        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + jwtService.generateToken(userId)))
                .andExpect(status().isForbidden());
    }
}
