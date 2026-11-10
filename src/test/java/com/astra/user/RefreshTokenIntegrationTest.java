package com.astra.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class RefreshTokenIntegrationTest {

    private static final String COOKIE = "astra_refresh_token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private MockHttpServletResponse login() throws Exception {
        String email = "refresh-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sessao\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        return mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.12." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    private MockHttpServletResponse refresh(Cookie cookie) throws Exception {
        return mockMvc.perform(post("/auth/refresh").cookie(cookie)).andReturn().getResponse();
    }

    private void newRequest() {
        entityManager.flush();
        entityManager.clear();
    }

    private void ageRevocationOf(Cookie cookie) throws Exception {
        entityManager.flush();
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(cookie.getValue().getBytes(StandardCharsets.UTF_8)));
        jdbcTemplate.update("update refresh_token set revoked_at = now() - interval '1 minute' where token_hash = ?",
                hash);
        entityManager.clear();
    }

    private void ageRevocation() {
        entityManager.flush();
        jdbcTemplate.update("update refresh_token set revoked_at = now() - interval '1 minute' "
                + "where revoked_at is not null");
        entityManager.clear();
    }

    @Test
    void cookieDeSessaoSoTrafegaEmHttpsNaRotaDeAuth() throws Exception {
        String header = login().getHeader("Set-Cookie");

        assertThat(header).startsWith(COOKIE + "=")
                .contains("Path=/auth", "HttpOnly", "Secure", "SameSite=None", "Max-Age=2592000");
    }

    @Test
    void renovarTrocaOTokenEDevolveUsuarioEAcessoNovos() throws Exception {
        Cookie original = login().getCookie(COOKIE);

        MockHttpServletResponse renewed = refresh(original);

        assertThat(renewed.getStatus()).isEqualTo(200);
        Cookie next = renewed.getCookie(COOKIE);
        assertThat(next.getValue()).isNotEqualTo(original.getValue());
        mockMvc.perform(post("/auth/refresh").cookie(next))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.name").value("Sessao"));
    }

    @Test
    void tokenVelhoReusadoDepoisDaToleranciaDerrubaTodasAsSessoes() throws Exception {
        Cookie original = login().getCookie(COOKIE);
        Cookie next = refresh(original).getCookie(COOKIE);
        ageRevocation();

        assertThat(refresh(original).getStatus()).isEqualTo(401);
        newRequest();
        assertThat(refresh(next).getStatus()).isEqualTo(401);
    }

    @Test
    void tokenExpiradoNaoRenova() throws Exception {
        Cookie original = login().getCookie(COOKIE);
        entityManager.flush();
        jdbcTemplate.update("update refresh_token set expires_at = now() - interval '1 minute'");
        newRequest();

        assertThat(refresh(original).getStatus()).isEqualTo(401);
    }

    @Test
    void logoutRevogaOTokenELimpaOCookie() throws Exception {
        Cookie original = login().getCookie(COOKIE);

        MockHttpServletResponse logout = mockMvc.perform(post("/auth/logout").cookie(original))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
        assertThat(logout.getHeader("Set-Cookie")).contains(COOKIE + "=", "Max-Age=0", "Path=/auth");
        ageRevocation();

        assertThat(refresh(original).getStatus()).isEqualTo(401);
        mockMvc.perform(post("/auth/logout")).andExpect(status().isNoContent());
        mockMvc.perform(post("/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void depoisDoLogoutNemDentroDaToleranciaRenova() throws Exception {
        Cookie original = login().getCookie(COOKIE);

        mockMvc.perform(post("/auth/logout").cookie(original)).andExpect(status().isNoContent());
        newRequest();

        assertThat(refresh(original).getStatus()).isEqualTo(401);
    }

    @Test
    void revogacaoGeralDerrubaAteOTokenQueAcabouDeSerRotacionado() throws Exception {
        Cookie original = login().getCookie(COOKIE);
        Cookie second = refresh(original).getCookie(COOKIE);
        Cookie third = refresh(second).getCookie(COOKIE);
        ageRevocationOf(original);

        assertThat(refresh(original).getStatus()).isEqualTo(401);
        newRequest();

        assertThat(refresh(second).getStatus()).isEqualTo(401);
        newRequest();
        assertThat(refresh(third).getStatus()).isEqualTo(401);
    }

    @Test
    void duasAbasRenovandoJuntasContinuamFuncionando() throws Exception {
        Cookie original = login().getCookie(COOKIE);

        assertThat(refresh(original).getStatus()).isEqualTo(200);
        newRequest();
        assertThat(refresh(original).getStatus()).isEqualTo(200);
    }
}
