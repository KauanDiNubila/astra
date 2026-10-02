package com.astra.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthRefreshIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult registerAndLogin() throws Exception {
        String email = "refresh-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Refresh\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        return mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.1." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void refreshConcorrenteComOMesmoTokenNaoDerrubaASessao() throws Exception {
        Cookie original = registerAndLogin().getResponse().getCookie("astra_refresh_token");

        mockMvc.perform(post("/auth/refresh").cookie(original)).andExpect(status().isOk());
        mockMvc.perform(post("/auth/refresh").cookie(original)).andExpect(status().isOk());
    }

    @Test
    void tokenInexistenteRetorna401() throws Exception {
        mockMvc.perform(post("/auth/refresh").cookie(new Cookie("astra_refresh_token", "inexistente")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void avatarDeUsuarioSemFotoRetorna204() throws Exception {
        MvcResult login = registerAndLogin();
        String userId = JsonPath.read(login.getResponse().getContentAsString(), "$.user.id");

        mockMvc.perform(get("/users/" + userId + "/avatar")).andExpect(status().isNoContent());
    }
}
