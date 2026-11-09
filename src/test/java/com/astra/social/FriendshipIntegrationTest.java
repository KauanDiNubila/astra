package com.astra.social;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class FriendshipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private record TestUser(String token, String id, String name, String handle) {
    }

    private TestUser newUser() throws Exception {
        String name = "U" + UUID.randomUUID().toString().substring(0, 8);
        String email = "fr-" + UUID.randomUUID() + "@astra.local";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.5." + ThreadLocalRandom.current().nextInt(256) + "." + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");
        String me = mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String tag = JsonPath.read(me, "$.tag");
        return new TestUser(token, JsonPath.read(me, "$.id"), name, name + "#" + tag);
    }

    private ResultActions invite(TestUser from, String handle) throws Exception {
        return mockMvc.perform(post("/friends")
                .header("Authorization", "Bearer " + from.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"handle\":\"" + handle + "\"}"));
    }

    private String inviteId(TestUser from, TestUser to) throws Exception {
        String body = invite(from, to.handle()).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private ResultActions accept(TestUser who, String friendshipId) throws Exception {
        return mockMvc.perform(post("/friends/{id}/accept", friendshipId)
                .header("Authorization", "Bearer " + who.token()));
    }

    private ResultActions friends(TestUser who) throws Exception {
        return mockMvc.perform(get("/friends").header("Authorization", "Bearer " + who.token()));
    }

    private ResultActions requests(TestUser who) throws Exception {
        return mockMvc.perform(get("/friends/requests").header("Authorization", "Bearer " + who.token()));
    }

    @Test
    void convitePendenteApareceParaOsDoisLados() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();

        invite(ana, bia.handle())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.friendUserId").value(bia.id()))
                .andExpect(jsonPath("$.friendName").value(bia.name()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.incoming").value(false))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        requests(bia).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].friendUserId").value(ana.id()))
                .andExpect(jsonPath("$[0].incoming").value(true));
        requests(ana).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].incoming").value(false));
        friends(ana).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void convidadoAceitaEViramAmigos() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        String id = inviteId(ana, bia);

        accept(bia, id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.friendUserId").value(ana.id()));

        friends(ana).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].friendUserId").value(bia.id()));
        friends(bia).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].friendUserId").value(ana.id()));
        requests(ana).andExpect(jsonPath("$", hasSize(0)));
        requests(bia).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void quemConvidouOuTerceiroNaoAceita() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        String id = inviteId(ana, bia);

        accept(ana, id).andExpect(status().isNotFound());
        accept(newUser(), id).andExpect(status().isNotFound());
        accept(bia, UUID.randomUUID().toString()).andExpect(status().isNotFound());

        requests(bia).andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void conviteCruzadoAceitaDireto() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        inviteId(ana, bia);

        invite(bia, ana.handle())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.friendUserId").value(ana.id()));

        friends(ana).andExpect(jsonPath("$", hasSize(1)));
        friends(bia).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void conviteRepetidoEJaAmigosRetornam409() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        String id = inviteId(ana, bia);

        invite(ana, bia.handle()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Convite já enviado"));

        accept(bia, id).andExpect(status().isOk());
        invite(ana, bia.handle()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vocês já são amigos"));
        invite(bia, ana.handle()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vocês já são amigos"));
    }

    @Test
    void naoAdicionaASiMesmo() throws Exception {
        TestUser ana = newUser();

        invite(ana, ana.handle()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível adicionar você mesmo"));
    }

    @Test
    void handleInexistenteRetorna404EFormatoInvalido400() throws Exception {
        TestUser ana = newUser();

        invite(ana, "ninguem-" + UUID.randomUUID().toString().substring(0, 6) + "#0001")
                .andExpect(status().isNotFound());
        invite(ana, "sem-tag").andExpect(status().isBadRequest());
        invite(ana, "nome#12").andExpect(status().isBadRequest());
        invite(ana, "").andExpect(status().isBadRequest());
    }

    @Test
    void qualquerUmDosDoisDesfazAmizadeEConvite() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        String friendship = inviteId(ana, bia);
        accept(bia, friendship).andExpect(status().isOk());

        mockMvc.perform(delete("/friends/{id}", friendship).header("Authorization", "Bearer " + bia.token()))
                .andExpect(status().isNoContent());
        friends(ana).andExpect(jsonPath("$", hasSize(0)));

        TestUser caio = newUser();
        String pending = inviteId(ana, caio);
        mockMvc.perform(delete("/friends/{id}", pending).header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isNoContent());
        requests(caio).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void terceiroNaoDesfazEInexistenteRetorna404() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        String id = inviteId(ana, bia);

        mockMvc.perform(delete("/friends/{id}", id).header("Authorization", "Bearer " + newUser().token()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/friends/{id}", UUID.randomUUID()).header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isNotFound());

        requests(bia).andExpect(jsonPath("$", hasSize(1)));
    }

    private void studyNow(TestUser who, int minutes) throws Exception {
        String category = mockMvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + who.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat-" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/sessions")
                        .header("Authorization", "Bearer " + who.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"" + JsonPath.read(category, "$.id") + "\",\"focusedMinutes\":"
                                + minutes + ",\"startedAt\":\"" + java.time.OffsetDateTime.now() + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void rankingDeAmigosTemSoOsAmigosAceitosEEuMesmo() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        TestUser caio = newUser();
        TestUser dani = newUser();
        accept(bia, inviteId(ana, bia)).andExpect(status().isOk());
        inviteId(ana, caio);
        studyNow(ana, 30);
        studyNow(bia, 50);
        studyNow(caio, 90);
        studyNow(dani, 120);

        mockMvc.perform(get("/ranking").param("period", "DAILY").param("scope", "FRIENDS")
                        .header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value(bia.id()))
                .andExpect(jsonPath("$[0].minutes").value(50))
                .andExpect(jsonPath("$[1].userId").value(ana.id()))
                .andExpect(jsonPath("$[1].position").value(2));
    }

    @Test
    void exportacaoTrazAmizadesENomeComTag() throws Exception {
        TestUser ana = newUser();
        TestUser bia = newUser();
        TestUser caio = newUser();
        accept(bia, inviteId(ana, bia)).andExpect(status().isOk());
        inviteId(caio, ana);

        mockMvc.perform(get("/me/export").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendships", hasSize(2)))
                .andExpect(jsonPath("$.friendships[?(@.userId == '" + bia.id() + "')].name").value(bia.handle()))
                .andExpect(jsonPath("$.friendships[?(@.userId == '" + bia.id() + "')].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.friendships[?(@.userId == '" + bia.id() + "')].sentByMe").value(true))
                .andExpect(jsonPath("$.friendships[?(@.userId == '" + caio.id() + "')].status").value("PENDING"))
                .andExpect(jsonPath("$.friendships[?(@.userId == '" + caio.id() + "')].sentByMe").value(false));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/friends")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/friends/requests")).andExpect(status().isUnauthorized());
    }
}
