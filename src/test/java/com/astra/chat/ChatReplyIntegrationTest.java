package com.astra.chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ChatReplyIntegrationTest {

    private static final MockMultipartFile IMAGE =
            new MockMultipartFile("file", "a.png", "image/png", new byte[] {(byte) 0x89, 'P', 'N', 'G'});

    @Autowired
    private MockMvc mockMvc;

    private record TestUser(UUID id, String token, String handle) {
    }

    private TestUser newUser() throws Exception {
        String email = "reply-" + UUID.randomUUID() + "@astra.local";
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Reply\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.13." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new TestUser(UUID.fromString(JsonPath.read(registered, "$.id")), JsonPath.read(login, "$.accessToken"),
                "Reply#" + JsonPath.read(registered, "$.tag"));
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

    private ResultActions imageTo(TestUser sender, String path, UUID replyTo) throws Exception {
        var request = multipart(path).file(IMAGE).header("Authorization", "Bearer " + sender.token());
        if (replyTo != null) {
            request.param("replyToMessageId", replyTo.toString());
        }
        return mockMvc.perform(request);
    }

    private UUID idOf(ResultActions result) throws Exception {
        return UUID.fromString(JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id"));
    }

    @Test
    void respostaNaConversaPrivadaTrazAPreviaDaOriginal() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        befriend(ana, beto);
        UUID original = idOf(imageTo(ana, "/chat/" + beto.id() + "/messages/image", null).andExpect(status().isOk()));

        imageTo(beto, "/chat/" + ana.id() + "/messages/image", original)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replyTo.id").value(original.toString()))
                .andExpect(jsonPath("$.replyTo.senderId").value(ana.id().toString()));
    }

    @Test
    void responderNaConversaPrivadaAMensagemDeGrupoRetorna409() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        befriend(ana, beto);
        UUID groupId = group(ana, beto);
        UUID groupMessage = idOf(imageTo(ana, "/chat/groups/" + groupId + "/messages/image", null)
                .andExpect(status().isOk()));

        imageTo(ana, "/chat/" + beto.id() + "/messages/image", groupMessage).andExpect(status().isConflict());
        imageTo(beto, "/chat/" + ana.id() + "/messages/image", groupMessage).andExpect(status().isConflict());
    }

    @Test
    void responderNoGrupoAMensagemPrivadaOuDeOutraConversaRetorna409() throws Exception {
        TestUser ana = newUser();
        TestUser beto = newUser();
        TestUser caio = newUser();
        befriend(ana, beto);
        befriend(ana, caio);
        UUID groupId = group(ana, beto);
        UUID direct = idOf(imageTo(ana, "/chat/" + beto.id() + "/messages/image", null).andExpect(status().isOk()));
        UUID otherConversation = idOf(imageTo(ana, "/chat/" + caio.id() + "/messages/image", null)
                .andExpect(status().isOk()));

        imageTo(ana, "/chat/groups/" + groupId + "/messages/image", direct).andExpect(status().isConflict());
        imageTo(ana, "/chat/" + beto.id() + "/messages/image", otherConversation).andExpect(status().isConflict());
        imageTo(ana, "/chat/" + beto.id() + "/messages/image", UUID.randomUUID()).andExpect(status().isNotFound());
    }
}
