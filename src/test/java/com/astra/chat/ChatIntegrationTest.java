package com.astra.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ChatIntegrationTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private record TestUser(UUID id, String token, String handle, String name) {
    }

    private TestUser newUser(String name) throws Exception {
        String email = "chat-" + UUID.randomUUID() + "@astra.local";
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email
                                + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.14." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new TestUser(UUID.fromString(JsonPath.read(registered, "$.id")), JsonPath.read(login, "$.accessToken"),
                name + "#" + JsonPath.read(registered, "$.tag"), name);
    }

    private void befriend(TestUser a, TestUser b) throws Exception {
        String sent = call(a, post("/friends"), "{\"handle\":\"" + b.handle() + "\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        call(b, post("/friends/{id}/accept", (String) JsonPath.read(sent, "$.id")), null).andExpect(status().isOk());
    }

    private void newRequest() {
        entityManager.flush();
        entityManager.clear();
    }

    private ResultActions call(TestUser user, MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header("Authorization", "Bearer " + user.token());
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    private ResultActions image(TestUser sender, String path, String caption, MockMultipartFile file) throws Exception {
        var request = multipart(path).file(file).header("Authorization", "Bearer " + sender.token());
        if (caption != null) {
            request.param("caption", caption);
        }
        return mockMvc.perform(request);
    }

    private ResultActions photo(TestUser sender, String path, String caption) throws Exception {
        return image(sender, path, caption, new MockMultipartFile("file", "a.png", "image/png", PNG));
    }

    private UUID idOf(ResultActions result) throws Exception {
        return UUID.fromString(JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id"));
    }

    private UUID group(TestUser owner, List<TestUser> members) throws Exception {
        String ids = String.join(",", members.stream().map(m -> "\"" + m.id() + "\"").toList());
        String body = call(owner, post("/chat/groups"), "{\"name\":\" Estudo \",\"memberIds\":[" + ids + "]}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.groupId"));
    }

    @Test
    void conversaPrivadaContaNaoLidasMostraUltimaMensagemEMarcaComoLida() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser caio = newUser("Caio");
        befriend(beto, ana);
        befriend(beto, caio);

        photo(ana, "/chat/" + beto.id() + "/messages/image", "oi beto").andExpect(status().isOk())
                .andExpect(jsonPath("$.senderId").value(ana.id().toString()))
                .andExpect(jsonPath("$.recipientId").value(beto.id().toString()))
                .andExpect(jsonPath("$.content").value("oi beto"))
                .andExpect(jsonPath("$.attachmentId").exists())
                .andExpect(jsonPath("$.read").value(false));
        photo(ana, "/chat/" + beto.id() + "/messages/image", null).andExpect(status().isOk());

        call(beto, get("/chat/conversations"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].friendUserId").value(ana.id().toString()))
                .andExpect(jsonPath("$[0].friendName").value("Ana"))
                .andExpect(jsonPath("$[0].lastMessage").value("📷 Foto"))
                .andExpect(jsonPath("$[0].lastMessageAt").exists())
                .andExpect(jsonPath("$[0].unreadCount").value(2))
                .andExpect(jsonPath("$[1].friendUserId").value(caio.id().toString()))
                .andExpect(jsonPath("$[1].lastMessage").doesNotExist())
                .andExpect(jsonPath("$[1].unreadCount").value(0));
        call(beto, get("/chat/unread-count"), null).andExpect(content().string("2"));

        call(beto, post("/chat/{id}/read", ana.id()), null).andExpect(status().isNoContent());
        call(beto, get("/chat/unread-count"), null).andExpect(content().string("0"));
        newRequest();
        call(ana, get("/chat/{id}/messages", beto.id()), null)
                .andExpect(jsonPath("$[0].read").value(true));
    }

    @Test
    void conversaMostraOGithubDoAmigoSoQuandoEleDeixaVisivel() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        befriend(ana, beto);
        entityManager.flush();
        jdbcTemplate.update("insert into github_connection (user_id, github_user_id, github_login, github_avatar_url, "
                + "access_token, access_token_expires_at, visible_to_friends) values (?, 1, 'beto-dev', "
                + "'https://avatars/beto', 'x', now(), false)", beto.id());

        call(ana, get("/chat/conversations"), null)
                .andExpect(jsonPath("$[0].friendGithubLogin").doesNotExist());

        jdbcTemplate.update("update github_connection set visible_to_friends = true where user_id = ?", beto.id());
        newRequest();
        call(ana, get("/chat/conversations"), null)
                .andExpect(jsonPath("$[0].friendGithubLogin").value("beto-dev"))
                .andExpect(jsonPath("$[0].friendGithubAvatarUrl").value("https://avatars/beto"));
    }

    @Test
    void historicoPrivadoEmOrdemSoEntreAmigos() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser estranho = newUser("Estranho");
        befriend(ana, beto);
        photo(ana, "/chat/" + beto.id() + "/messages/image", "primeira").andExpect(status().isOk());
        photo(beto, "/chat/" + ana.id() + "/messages/image", "segunda").andExpect(status().isOk());

        call(ana, get("/chat/{id}/messages", beto.id()), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("primeira"))
                .andExpect(jsonPath("$[1].content").value("segunda"));
        call(ana, get("/chat/{id}/messages", beto.id()).param("limit", "1"), null)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("segunda"));

        call(estranho, get("/chat/{id}/messages", ana.id()), null).andExpect(status().isNotFound());
        photo(estranho, "/chat/" + ana.id() + "/messages/image", "oi").andExpect(status().isNotFound());
    }

    @Test
    void imagemPrecisaSerImagemDeAte2MBEAnexoSoParaQuemEstaNaConversa() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser estranho = newUser("Estranho");
        befriend(ana, beto);
        String path = "/chat/" + beto.id() + "/messages/image";

        image(ana, path, null, new MockMultipartFile("file", "a.txt", "text/plain", PNG))
                .andExpect(status().isConflict());
        image(ana, path, null, new MockMultipartFile("file", "a.png", "image/png", new byte[2 * 1024 * 1024 + 1]))
                .andExpect(status().isConflict());
        UUID messageId = idOf(photo(ana, path, "foto").andExpect(status().isOk()));

        call(beto, get("/chat/messages/{id}/attachment", messageId), null)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(PNG));
        call(estranho, get("/chat/messages/{id}/attachment", messageId), null).andExpect(status().isNotFound());
        call(ana, get("/chat/messages/{id}/attachment", UUID.randomUUID()), null).andExpect(status().isNotFound());
    }

    @Test
    void grupoSoComAmigosComNomeAparadoEMembros() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser estranho = newUser("Estranho");
        befriend(ana, beto);

        call(ana, post("/chat/groups"), "{\"name\":\"X\",\"memberIds\":[\"" + estranho.id() + "\"]}")
                .andExpect(status().isConflict());
        call(ana, post("/chat/groups"), "{\"name\":\" \",\"memberIds\":[\"" + beto.id() + "\"]}")
                .andExpect(status().isBadRequest());
        call(ana, post("/chat/groups"), "{\"name\":\"X\",\"memberIds\":[]}").andExpect(status().isBadRequest());

        call(ana, post("/chat/groups"), "{\"name\":\" Estudo \",\"memberIds\":[\"" + beto.id() + "\",\""
                + beto.id() + "\",\"" + ana.id() + "\"]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupName").value("Estudo"))
                .andExpect(jsonPath("$.memberNames.length()").value(2))
                .andExpect(jsonPath("$.unreadCount").value(0));
        UUID groupId = group(ana, List.of(beto));

        call(beto, get("/chat/groups/{id}/members", groupId), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.userId == '" + ana.id() + "')].name").value("Ana"));
        call(estranho, get("/chat/groups/{id}/members", groupId), null).andExpect(status().isNotFound());
    }

    @Test
    void membroAdicionaSoAmigoQueAindaNaoEstaNoGrupo() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser caio = newUser("Caio");
        TestUser estranho = newUser("Estranho");
        befriend(ana, beto);
        befriend(beto, caio);
        UUID groupId = group(ana, List.of(beto));

        call(ana, post("/chat/groups/{id}/members", groupId), "{\"userId\":\"" + caio.id() + "\"}")
                .andExpect(status().isConflict());
        call(beto, post("/chat/groups/{id}/members", groupId), "{\"userId\":\"" + ana.id() + "\"}")
                .andExpect(status().isConflict());
        call(estranho, post("/chat/groups/{id}/members", groupId), "{\"userId\":\"" + caio.id() + "\"}")
                .andExpect(status().isNotFound());
        call(beto, post("/chat/groups/{id}/members", groupId), "{\"userId\":\"" + caio.id() + "\"}")
                .andExpect(status().isNoContent());

        call(caio, get("/chat/groups/{id}/members", groupId), null).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void conversasDeGrupoContamNaoLidasEMarcamComoLidas() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser estranho = newUser("Estranho");
        befriend(ana, beto);
        UUID groupId = group(ana, List.of(beto));
        entityManager.flush();
        jdbcTemplate.update("update chat_group_member set last_read_at = now() - interval '1 minute'");

        photo(ana, "/chat/groups/" + groupId + "/messages/image", "bom dia grupo").andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.recipientId").doesNotExist());

        call(beto, get("/chat/groups"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupId").value(groupId.toString()))
                .andExpect(jsonPath("$[0].lastMessage").value("bom dia grupo"))
                .andExpect(jsonPath("$[0].unreadCount").value(1));
        call(beto, get("/chat/groups/unread-count"), null).andExpect(content().string("1"));
        call(ana, get("/chat/groups/unread-count"), null).andExpect(content().string("0"));
        call(beto, get("/chat/groups/{id}/messages", groupId), null)
                .andExpect(jsonPath("$[0].content").value("bom dia grupo"));

        call(beto, post("/chat/groups/{id}/read", groupId), null).andExpect(status().isNoContent());
        call(beto, get("/chat/groups/unread-count"), null).andExpect(content().string("0"));

        call(estranho, get("/chat/groups/{id}/messages", groupId), null).andExpect(status().isNotFound());
        call(estranho, post("/chat/groups/{id}/read", groupId), null).andExpect(status().isNotFound());
        photo(estranho, "/chat/groups/" + groupId + "/messages/image", "x").andExpect(status().isNotFound());
        call(estranho, get("/chat/groups"), null).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void fotoDoGrupoSoParaMembrosESoImagem() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        TestUser estranho = newUser("Estranho");
        befriend(ana, beto);
        UUID groupId = group(ana, List.of(beto));
        String path = "/chat/groups/" + groupId + "/avatar";

        call(beto, get(path), null).andExpect(status().isNotFound());
        image(beto, path, null, new MockMultipartFile("file", "a.txt", "text/plain", PNG))
                .andExpect(status().isConflict());
        image(estranho, path, null, new MockMultipartFile("file", "a.png", "image/png", PNG))
                .andExpect(status().isNotFound());
        image(beto, path, null, new MockMultipartFile("file", "a.png", "image/png", PNG))
                .andExpect(status().isNoContent());

        call(ana, get(path), null)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(PNG));
        call(estranho, get(path), null).andExpect(status().isNotFound());
    }

    @Test
    void exportacaoTrazMensagensComFotoSoDasMinhasEGrupos() throws Exception {
        TestUser ana = newUser("Ana");
        TestUser beto = newUser("Beto");
        befriend(ana, beto);
        photo(ana, "/chat/" + beto.id() + "/messages/image", "minha").andExpect(status().isOk());
        photo(beto, "/chat/" + ana.id() + "/messages/image", "dele").andExpect(status().isOk());
        UUID groupId = group(ana, List.of(beto));

        String body = call(ana, get("/me/export"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[?(@.content == 'minha')].sentByMe").value(true))
                .andExpect(jsonPath("$.messages[?(@.content == 'minha')].image.contentType").value("image/png"))
                .andExpect(jsonPath("$.messages[?(@.content == 'dele')].sentByMe").value(false))
                .andExpect(jsonPath("$.groups.length()").value(1))
                .andExpect(jsonPath("$.groups[0].groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.groups[0].name").value("Estudo"))
                .andExpect(jsonPath("$.groups[0].createdByMe").value(true))
                .andReturn().getResponse().getContentAsString();

        List<String> mine = JsonPath.read(body, "$.messages[?(@.content == 'minha')].image.base64");
        List<Object> theirs = JsonPath.read(body, "$.messages[?(@.content == 'dele')].image.base64");
        assertThat(mine).singleElement().isNotNull();
        assertThat(theirs).isEmpty();
    }
}
