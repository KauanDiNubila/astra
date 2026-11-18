package com.astra.user;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.user.application.port.out.SendPasswordResetEmailPort;
import com.astra.user.domain.PasswordResetToken;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class PasswordResetIntegrationTest {

    private static final String PASSWORD = "Xk9$mQ2vN8pL4wR7";
    private static final String NEW_PASSWORD = "Qz7#nB4tW1sK9pL2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SendPasswordResetEmailPort sendPasswordResetEmailPort;

    private ResultActions postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions login(String email, String password) throws Exception {
        return postJson("/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    private String requestResetLink(String email) throws Exception {
        postJson("/auth/forgot-password", "{\"email\":\"" + email + "\"}").andExpect(status().isNoContent());
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(sendPasswordResetEmailPort).sendPasswordReset(eq(email), token.capture());
        return token.getValue();
    }

    private ResultActions reset(String token, String password) throws Exception {
        return postJson("/auth/reset-password", "{\"token\":\"" + token + "\",\"newPassword\":\"" + password + "\"}");
    }

    @Test
    void resetLinkChangesPasswordOnceAndEndsOldSessions() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@astra.local";
        postJson("/auth/register", "{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isCreated());
        Cookie oldSession = login(email, PASSWORD).andExpect(status().isOk())
                .andReturn().getResponse().getCookie("astra_refresh_token");

        String token = requestResetLink(email);
        reset(token, NEW_PASSWORD).andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        login(email, NEW_PASSWORD).andExpect(status().isOk());
        login(email, PASSWORD).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh").cookie(oldSession)).andExpect(status().isUnauthorized());
        reset(token, "Wm3!pR8xT5vB2nQ6").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(PasswordResetToken.INVALID_LINK));
    }

    @Test
    void unknownEmailGetsTheSameAnswerAndNoEmail() throws Exception {
        postJson("/auth/forgot-password", "{\"email\":\"ninguem-" + UUID.randomUUID() + "@astra.local\"}")
                .andExpect(status().isNoContent());

        verify(sendPasswordResetEmailPort, never()).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        reset("nao-existe", NEW_PASSWORD).andExpect(status().isConflict());
    }
}
