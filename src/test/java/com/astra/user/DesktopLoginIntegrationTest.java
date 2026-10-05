package com.astra.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.shared.security.AstraOAuthPrincipal;
import com.astra.shared.security.OAuthAuthenticationSuccessHandler;
import com.astra.user.service.DesktopLoginService;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "astra.desktop-login.code-ttl-seconds=1")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class DesktopLoginIntegrationTest {

    private static final Pattern CODE_IN_PAGE = Pattern.compile("href=\"astra://auth\\?code=([A-Za-z0-9_-]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuthAuthenticationSuccessHandler successHandler;

    @Autowired
    private DesktopLoginService desktopLoginService;

    private record Pkce(String verifier, String challenge) {
    }

    private Pkce newPkce() throws Exception {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return new Pkce(verifier, Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
    }

    private UUID registerUser() throws Exception {
        String email = "desktop-" + UUID.randomUUID() + "@astra.local";
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Desk\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private String exchangeBody(String code, String verifier) {
        return "{\"code\":\"" + code + "\",\"verifier\":\"" + verifier + "\"}";
    }

    @Test
    void iniciarLoginGuardaODesafioNaSessaoERedirecionaParaOProvedor() throws Exception {
        Pkce pkce = newPkce();

        var result = mockMvc.perform(get("/auth/desktop/login")
                        .param("provider", "google")
                        .param("challenge", pkce.challenge()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(DesktopLoginService.SESSION_ATTRIBUTE)).isEqualTo(pkce.challenge());
    }

    @Test
    void iniciarLoginRecusaProvedorEDesafioInvalidos() throws Exception {
        Pkce pkce = newPkce();

        mockMvc.perform(get("/auth/desktop/login").param("provider", "facebook").param("challenge", pkce.challenge()))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/auth/desktop/login").param("provider", "google").param("challenge", "curto"))
                .andExpect(status().isConflict());
    }

    @Test
    void depoisDoOAuthODesktopRecebeCodigoSemCookieETrocaPorSessao() throws Exception {
        UUID userId = registerUser();
        Pkce pkce = newPkce();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(DesktopLoginService.SESSION_ATTRIBUTE, pkce.challenge());
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(request, response,
                new TestingAuthenticationToken((AstraOAuthPrincipal) () -> userId, null));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Set-Cookie")).isNull();
        assertThat(session.getAttribute(DesktopLoginService.SESSION_ATTRIBUTE)).isNull();
        Matcher matcher = CODE_IN_PAGE.matcher(response.getContentAsString());
        assertThat(matcher.find()).isTrue();
        String code = matcher.group(1);

        mockMvc.perform(post("/auth/desktop/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody(code, pkce.verifier())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(userId.toString()))
                .andExpect(cookie().exists("astra_refresh_token"))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=None")));
    }

    @Test
    void oLoginNormalDoNavegadorContinuaDandoCookieERedirecionando() throws Exception {
        UUID userId = registerUser();

        MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                new TestingAuthenticationToken((AstraOAuthPrincipal) () -> userId, null));

        assertThat(response.getRedirectedUrl()).endsWith("/dashboard");
        assertThat(response.getHeader("Set-Cookie")).contains("astra_refresh_token");
    }

    @Test
    void codigoSoPodeSerUsadoUmaVez() throws Exception {
        UUID userId = registerUser();
        Pkce pkce = newPkce();
        String code = desktopLoginService.createCode(userId, pkce.challenge());

        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody(code, pkce.verifier())))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody(code, pkce.verifier())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verificadorErradoRecusaEQueimaOCodigo() throws Exception {
        UUID userId = registerUser();
        Pkce pkce = newPkce();
        Pkce other = newPkce();
        String code = desktopLoginService.createCode(userId, pkce.challenge());

        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody(code, other.verifier())))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("astra_refresh_token"));
        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody(code, pkce.verifier())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void codigoExpiraRapido() throws Exception {
        UUID userId = registerUser();
        Pkce pkce = newPkce();
        String code = desktopLoginService.createCode(userId, pkce.challenge());

        Thread.sleep(1300);

        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody(code, pkce.verifier())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void codigoInventadoEPedidoMalFormadoSaoRecusados() throws Exception {
        Pkce pkce = newPkce();

        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeBody("codigo-que-nao-existe", pkce.verifier())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/desktop/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"x\",\"verifier\":\"curto\"}"))
                .andExpect(status().isBadRequest());
    }
}
