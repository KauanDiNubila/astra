package com.astra.user.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.shared.crypto.JwtService;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.security.Principal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class StompBanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StompAuthChannelInterceptor interceptor;

    private UUID register() throws Exception {
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Stomp\",\"email\":\"stomp-" + UUID.randomUUID()
                                + "@astra.local\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.id"));
    }

    private void ban(UUID userId) {
        entityManager.flush();
        jdbcTemplate.update("update users set banned_at = now() where id = ?", userId);
        entityManager.clear();
    }

    private Message<byte[]> connect(UUID userId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + jwtService.generateToken(userId));
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> send(UUID userId, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination(destination);
        accessor.setUser((Principal) userId::toString);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void banidoNaoConecta() throws Exception {
        UUID userId = register();
        assertThat(interceptor.preSend(connect(userId), null)).isNotNull();

        ban(userId);

        assertThatThrownBy(() -> interceptor.preSend(connect(userId), null))
                .isInstanceOf(StompAuthenticationException.class);
    }

    @Test
    void banidoComConexaoAbertaNaoIniciaNemEntraEmChamada() throws Exception {
        UUID userId = register();
        assertThat(interceptor.preSend(send(userId, "/app/call.start"), null)).isNotNull();

        ban(userId);

        assertThatThrownBy(() -> interceptor.preSend(send(userId, "/app/call.start"), null))
                .isInstanceOf(StompAuthenticationException.class);
        assertThatThrownBy(() -> interceptor.preSend(send(userId, "/app/call.join"), null))
                .isInstanceOf(StompAuthenticationException.class);
        assertThat(interceptor.preSend(send(userId, "/app/call.leave"), null)).isNotNull();
    }
}
