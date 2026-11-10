package com.astra.user.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class BannedLoginIntegrationTest {

    private static final String PASSWORD = "Xk9$mQ2vN8pL4wR7";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OAuthAuthenticationSuccessHandler successHandler;

    private String register() throws Exception {
        String email = "banned-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Banido\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        return email;
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .with(request -> {
                    request.setRemoteAddr("10.9." + ThreadLocalRandom.current().nextInt(256) + "."
                            + ThreadLocalRandom.current().nextInt(1, 255));
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private UUID ban(String email) {
        entityManager.flush();
        jdbcTemplate.update("update users set banned_at = now() where email = ?", email);
        entityManager.clear();
        return jdbcTemplate.queryForObject("select id from users where email = ?", UUID.class, email);
    }

    @Test
    void banidoNaoRecebeTokensNoLogin() throws Exception {
        String email = register();
        ban(email);

        login(email, PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("astra_refresh_token"));
        login(email, "senha-errada").andExpect(status().isUnauthorized());
    }

    @Test
    void banidoNaoRenovaASessaoQueJaTinha() throws Exception {
        String email = register();
        Cookie refresh = login(email, PASSWORD).andExpect(status().isOk())
                .andReturn().getResponse().getCookie("astra_refresh_token");
        ban(email);

        mockMvc.perform(post("/auth/refresh").cookie(refresh))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("astra_refresh_token"));
    }

    @Test
    void banidoNaoEntraPeloGoogleOuGithubNemNoNavegadorNemNoDesktop() throws Exception {
        UUID userId = ban(register());

        MockHttpServletResponse browser = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(new MockHttpServletRequest(), browser,
                new TestingAuthenticationToken((AstraOAuthPrincipal) () -> userId, null));
        assertThat(browser.getRedirectedUrl()).endsWith("/login?oauthError=1");
        assertThat(browser.getHeader("Set-Cookie")).isNull();

        MockHttpServletRequest desktopRequest = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(OAuthAuthenticationSuccessHandler.DESKTOP_CHALLENGE, "x".repeat(43));
        desktopRequest.setSession(session);
        MockHttpServletResponse desktop = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(desktopRequest, desktop,
                new TestingAuthenticationToken((AstraOAuthPrincipal) () -> userId, null));
        assertThat(desktop.getRedirectedUrl()).endsWith("/login?oauthError=1");
        assertThat(desktop.getContentAsString()).doesNotContain("astra://auth");
        assertThat(session.getAttribute(OAuthAuthenticationSuccessHandler.DESKTOP_CHALLENGE)).isNull();
    }

    @Test
    void quemNaoEstaBanidoContinuaEntrando() throws Exception {
        String email = register();

        String body = login(email, PASSWORD).andExpect(status().isOk())
                .andExpect(cookie().exists("astra_refresh_token"))
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(body, "$.accessToken")).isNotBlank();
    }
}
