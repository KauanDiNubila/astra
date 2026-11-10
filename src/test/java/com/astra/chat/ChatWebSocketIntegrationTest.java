package com.astra.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.chat.controller.ChatWebSocketController;
import com.astra.chat.dto.SendGroupMessageRequest;
import com.astra.chat.dto.SendMessageRequest;
import com.jayway.jsonpath.JsonPath;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ChatWebSocketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatWebSocketController controller;

    @MockitoSpyBean
    private SimpMessagingTemplate messagingTemplate;

    private record TestUser(UUID id, String token, String handle) {
    }

    private TestUser newUser() throws Exception {
        String email = "ws-" + UUID.randomUUID() + "@astra.local";
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ws\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.15." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new TestUser(UUID.fromString(JsonPath.read(registered, "$.id")), JsonPath.read(login, "$.accessToken"),
                "Ws#" + JsonPath.read(registered, "$.tag"));
    }

    private void befriend(TestUser a, TestUser b) throws Exception {
        String sent = mockMvc.perform(post("/friends")
                        .header("Authorization", "Bearer " + a.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + b.handle() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/friends/{id}/accept", (String) JsonPath.read(sent, "$.id"))
                        .header("Authorization", "Bearer " + b.token()))
                .andExpect(status().isOk());
    }

    private UUID group(TestUser owner, TestUser member) throws Exception {
        String body = mockMvc.perform(post("/chat/groups")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Estudo\",\"memberIds\":[\"" + member.id() + "\"]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.groupId"));
    }

    private Principal principal(TestUser user) {
        return user.id()::toString;
    }

    private List<Object> sentTo(UUID userId, String destination) {
        List<Object> payloads = new ArrayList<>();
        Mockito.mockingDetails(messagingTemplate).getInvocations().forEach(invocation -> {
            Object[] args = invocation.getArguments();
            if (invocation.getMethod().getName().equals("convertAndSendToUser") && args.length == 3
                    && userId.toString().equals(args[0]) && destination.equals(args[1])) {
                payloads.add(args[2]);
            }
        });
        return payloads;
    }

    @Test
    void textoPrivadoChegaParaOsDoisESegurancaFicaLimpa() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        befriend(ana, beto);

        controller.send(new SendMessageRequest(beto.id(), "olá", null), principal(ana));

        assertThat(sentTo(beto.id(), "/queue/messages")).singleElement()
                .extracting(m -> ((com.astra.chat.dto.MessageResponse) m).content()).isEqualTo("olá");
        assertThat(sentTo(ana.id(), "/queue/messages")).hasSize(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void textoEmBrancoOuParaQuemNaoEAmigoVoltaComoErro() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        TestUser estranho = newUser();
        befriend(ana, beto);

        controller.send(new SendMessageRequest(beto.id(), "  ", null), principal(ana));
        controller.send(new SendMessageRequest(estranho.id(), "oi", null), principal(ana));

        assertThat(sentTo(ana.id(), "/queue/errors"))
                .containsExactly("Mensagem precisa ter texto", "Conversa não encontrada");
        assertThat(sentTo(beto.id(), "/queue/messages")).isEmpty();
        assertThat(sentTo(estranho.id(), "/queue/messages")).isEmpty();
    }

    @Test
    void textoNoGrupoChegaParaTodosOsMembros() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        TestUser estranho = newUser();
        befriend(ana, beto);
        UUID groupId = group(ana, beto);

        controller.sendGroup(new SendGroupMessageRequest(groupId, "bom dia", null), principal(beto));
        controller.sendGroup(new SendGroupMessageRequest(groupId, "oi", null), principal(estranho));

        assertThat(sentTo(ana.id(), "/queue/messages")).hasSize(1);
        assertThat(sentTo(beto.id(), "/queue/messages")).hasSize(1);
        assertThat(sentTo(estranho.id(), "/queue/errors")).containsExactly("Grupo não encontrado");
    }
}
