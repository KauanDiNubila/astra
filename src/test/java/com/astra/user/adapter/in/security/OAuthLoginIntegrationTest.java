package com.astra.user.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.shared.crypto.JwtService;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class OAuthLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleOidcUserService googleOidcUserService;

    private static OidcUser google(String subject, String email, String name) {
        OidcIdToken token = new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", subject, "email", email, "name", name, "email_verified", true));
        return new DefaultOidcUser(List.of(), token);
    }

    private UUID signInWithGoogle(String subject, String email, String name) {
        return ((AstraOAuthPrincipal) googleOidcUserService.toAstraUser("google", google(subject, email, name)))
                .getUserId();
    }

    private ResultActions call(UUID userId, MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("Authorization", "Bearer " + jwtService.generateToken(userId));
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    @Test
    void googleCriaContaComTermosAceitosEVoltaNaMesmaNasProximas() throws Exception {
        String email = "oauth-" + UUID.randomUUID() + "@astra.local";
        String subject = "sub-" + UUID.randomUUID();

        UUID first = signInWithGoogle(subject, email, "Pelo Google");
        UUID second = signInWithGoogle(subject, "outro-" + email, "Outro Nome");

        assertThat(second).isEqualTo(first);
        call(first, get("/me"), null)
                .andExpect(jsonPath("$.name").value("Pelo Google"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.hasPassword").value(false))
                .andExpect(jsonPath("$.termsAccepted").value(true));
        call(first, get("/me/export"), null)
                .andExpect(jsonPath("$.profile.loginProviders.length()").value(1))
                .andExpect(jsonPath("$.profile.loginProviders[0].provider").value("google"));
    }

    @Test
    void googleComEmailDeContaExistenteVinculaAEssaConta() throws Exception {
        String email = "existente-" + UUID.randomUUID() + "@astra.local";
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Existente\",\"email\":\"" + email
                                + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID existing = UUID.fromString(JsonPath.read(body, "$.id"));

        UUID linked = signInWithGoogle("sub-" + UUID.randomUUID(), email, "Qualquer");

        assertThat(linked).isEqualTo(existing);
        call(existing, get("/me"), null)
                .andExpect(jsonPath("$.name").value("Existente"))
                .andExpect(jsonPath("$.hasPassword").value(true));
    }

    @Test
    void contaSemSenhaNaoTrocaSenha() throws Exception {
        UUID userId = signInWithGoogle("sub-" + UUID.randomUUID(), "google-" + UUID.randomUUID() + "@astra.local",
                "Google");

        call(userId, put("/me/password"), "{\"currentPassword\":\"x\",\"newPassword\":\"Qz7#nB4tW1sK9pL2\"}")
                .andExpect(status().isConflict());
    }
}
