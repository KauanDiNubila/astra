package com.astra.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.chat.dto.MessageResponse;
import com.astra.chat.service.ChatService;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ChatPinIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatService chatService;

    @MockitoSpyBean
    private SimpMessagingTemplate messagingTemplate;

    private record TestUser(UUID id, String token, String handle) {
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    private TestUser newUser() throws Exception {
        String email = "pin-" + UUID.randomUUID() + "@astra.local";
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pinner\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.3." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(registered, "$.id");
        String tag = JsonPath.read(registered, "$.tag");
        return new TestUser(UUID.fromString(id), JsonPath.read(login, "$.accessToken"), "Pinner#" + tag);
    }

    private void befriend(TestUser a, TestUser b) throws Exception {
        String sent = mockMvc.perform(post("/friends")
                        .header("Authorization", "Bearer " + a.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + b.handle() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String friendshipId = JsonPath.read(sent, "$.id");
        mockMvc.perform(post("/friends/" + friendshipId + "/accept").header("Authorization", "Bearer " + b.token()))
                .andExpect(status().isOk());
    }

    private UUID createGroup(TestUser owner, List<TestUser> members) throws Exception {
        String ids = String.join(",", members.stream().map(m -> "\"" + m.id() + "\"").toList());
        String body = mockMvc.perform(post("/chat/groups")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Estudo\",\"memberIds\":[" + ids + "]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.groupId"));
    }

    private UUID sendAs(TestUser sender, UUID recipientId, String content) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(sender.id(), null, List.of()));
        try {
            return chatService.send(recipientId, content, null).id();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private UUID sendToGroupAs(TestUser sender, UUID groupId, String content) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(sender.id(), null, List.of()));
        try {
            return chatService.sendGroupMessage(groupId, content, null).id();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private List<MessageResponse> updatesFor(UUID userId) {
        List<MessageResponse> updates = new ArrayList<>();
        Mockito.mockingDetails(messagingTemplate).getInvocations().forEach(invocation -> {
            Object[] args = invocation.getArguments();
            if (invocation.getMethod().getName().equals("convertAndSendToUser") && args.length == 3
                    && userId.toString().equals(args[0]) && "/queue/message-updates".equals(args[1])
                    && args[2] instanceof MessageResponse message) {
                updates.add(message);
            }
        });
        return updates;
    }

    @Test
    void amigoFixaEDesafixaMensagemDaConversaEOsDoisSaoAvisados() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        befriend(ana, beto);
        UUID messageId = sendAs(ana, beto.id(), "Prova na sexta às 10h");

        mockMvc.perform(put("/chat/messages/" + messageId + "/pin").header("Authorization", "Bearer " + beto.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Prova na sexta às 10h"))
                .andExpect(jsonPath("$.pinnedBy").value(beto.id().toString()))
                .andExpect(jsonPath("$.pinnedAt").isNotEmpty());

        mockMvc.perform(get("/chat/" + beto.id() + "/pins").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(messageId.toString()));
        mockMvc.perform(get("/chat/" + ana.id() + "/messages").header("Authorization", "Bearer " + beto.token()))
                .andExpect(jsonPath("$[0].pinnedAt").isNotEmpty());

        assertThat(updatesFor(ana.id())).extracting(MessageResponse::id).containsExactly(messageId);
        assertThat(updatesFor(beto.id())).extracting(MessageResponse::id).containsExactly(messageId);

        mockMvc.perform(delete("/chat/messages/" + messageId + "/pin").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinnedAt").doesNotExist());
        mockMvc.perform(get("/chat/" + ana.id() + "/pins").header("Authorization", "Bearer " + beto.token()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void quemNaoParticipaDaConversaNaoFixa() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        TestUser intruso = newUser();
        befriend(ana, beto);
        UUID messageId = sendAs(ana, beto.id(), "Segredo");

        mockMvc.perform(put("/chat/messages/" + messageId + "/pin").header("Authorization", "Bearer " + intruso.token()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/chat/" + ana.id() + "/pins").header("Authorization", "Bearer " + intruso.token()))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/chat/messages/" + UUID.randomUUID() + "/pin").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void limiteDeMensagensFixadasPorConversaEOrdemDaMaisRecente() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        befriend(ana, beto);
        List<UUID> ids = new ArrayList<>();
        for (int i = 1; i <= ChatService.MAX_PINNED_PER_CONVERSATION + 1; i++) {
            ids.add(sendAs(ana, beto.id(), "Mensagem " + i));
        }
        for (int i = 0; i < ChatService.MAX_PINNED_PER_CONVERSATION; i++) {
            mockMvc.perform(put("/chat/messages/" + ids.get(i) + "/pin").header("Authorization", "Bearer " + ana.token()))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(put("/chat/messages/" + ids.get(0) + "/pin").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk());

        UUID extra = ids.get(ChatService.MAX_PINNED_PER_CONVERSATION);
        mockMvc.perform(put("/chat/messages/" + extra + "/pin").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/chat/" + beto.id() + "/pins").header("Authorization", "Bearer " + ana.token()))
                .andExpect(jsonPath("$.length()").value(ChatService.MAX_PINNED_PER_CONVERSATION))
                .andExpect(jsonPath("$[0].id").value(ids.get(ChatService.MAX_PINNED_PER_CONVERSATION - 1).toString()));
    }

    @Test
    void membroDoGrupoFixaETodosOsMembrosSaoAvisados() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        TestUser caio = newUser();
        TestUser fora = newUser();
        befriend(ana, beto);
        befriend(ana, caio);
        UUID groupId = createGroup(ana, List.of(beto, caio));
        UUID messageId = sendToGroupAs(beto, groupId, "Link da aula ao vivo");

        mockMvc.perform(put("/chat/messages/" + messageId + "/pin").header("Authorization", "Bearer " + fora.token()))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/chat/messages/" + messageId + "/pin").header("Authorization", "Bearer " + caio.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()));

        mockMvc.perform(get("/chat/groups/" + groupId + "/pins").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(messageId.toString()));
        mockMvc.perform(get("/chat/groups/" + groupId + "/pins").header("Authorization", "Bearer " + fora.token()))
                .andExpect(status().isNotFound());

        for (TestUser member : List.of(ana, beto, caio)) {
            assertThat(updatesFor(member.id())).extracting(MessageResponse::id).containsExactly(messageId);
        }
        assertThat(updatesFor(fora.id())).isEmpty();
    }
}
