package com.astra.call;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.call.dto.CallEvent;
import com.astra.call.dto.SignalRequest;
import com.astra.call.dto.StartCallRequest;
import com.astra.call.service.CallService;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "astra.call.turn-urls=turn:turn.test:3478?transport=udp,turn:turn.test:3478?transport=tcp")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class CallIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CallService callService;

    @MockitoSpyBean
    private SimpMessagingTemplate messagingTemplate;

    @Value("${astra.call.turn-secret}")
    private String turnSecret;

    private record TestUser(UUID id, String token, String handle) {
    }

    private TestUser newUser() throws Exception {
        String email = "call-" + UUID.randomUUID() + "@astra.local";
        String registered = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Caller\",\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String login = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("10.2." + ThreadLocalRandom.current().nextInt(256) + "."
                                    + ThreadLocalRandom.current().nextInt(1, 255));
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Xk9$mQ2vN8pL4wR7\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(registered, "$.id");
        String tag = JsonPath.read(registered, "$.tag");
        return new TestUser(UUID.fromString(id), JsonPath.read(login, "$.accessToken"), "Caller#" + tag);
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

    private List<CallEvent> eventsFor(UUID userId) {
        List<CallEvent> events = new ArrayList<>();
        Mockito.mockingDetails(messagingTemplate).getInvocations().forEach(invocation -> {
            Object[] args = invocation.getArguments();
            if (invocation.getMethod().getName().equals("convertAndSendToUser") && args.length == 3
                    && userId.toString().equals(args[0]) && "/queue/call".equals(args[1])
                    && args[2] instanceof CallEvent event) {
                events.add(event);
            }
        });
        return events;
    }

    private CallEvent lastEvent(UUID userId, String type) {
        List<CallEvent> events = eventsFor(userId).stream().filter(e -> e.type().equals(type)).toList();
        assertThat(events).as("evento %s para %s", type, userId).isNotEmpty();
        return events.get(events.size() - 1);
    }

    private StartCallRequest dm(UUID target, String clientId) {
        return new StartCallRequest(target, null, clientId);
    }

    @Test
    void iceServersExigeLoginEDevolveCredencialEfemeraValida() throws Exception {
        mockMvc.perform(get("/call/ice-servers")).andExpect(status().isUnauthorized());

        TestUser user = newUser();
        String body = mockMvc.perform(get("/call/ice-servers").header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iceServers[0].urls[0]", containsString("stun:")))
                .andExpect(jsonPath("$.iceServers[1].urls[0]", containsString("turn:turn.test")))
                .andReturn().getResponse().getContentAsString();

        String username = JsonPath.read(body, "$.iceServers[1].username");
        String credential = JsonPath.read(body, "$.iceServers[1].credential");
        long expiresAt = Long.parseLong(username.split(":")[0]);
        assertThat(username).endsWith(":" + user.id());
        assertThat(expiresAt - System.currentTimeMillis() / 1000).isBetween(43100L, 43201L);

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(turnSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        String expected = Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
        assertThat(credential).isEqualTo(expected);
    }

    @Test
    void naoAmigoNaoConsegueLigar() throws Exception {
        TestUser a = newUser();
        TestUser stranger = newUser();

        assertThatThrownBy(() -> callService.start(a.id(), "s1", dm(stranger.id(), "ca")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void ligacaoDiretaToca_atende_trocaSinalEEncerra() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        TestUser stranger = newUser();
        befriend(a, b);

        callService.start(a.id(), "sa", dm(b.id(), "ca"));
        CallEvent incoming = lastEvent(b.id(), "incoming");
        assertThat(incoming.userId()).isEqualTo(a.id());
        UUID callId = incoming.callId();

        callService.join(b.id(), "sb", callId, "cb");
        assertThat(lastEvent(a.id(), "joined").clientId()).isEqualTo("cb");
        assertThat(lastEvent(b.id(), "state").participants()).hasSize(2);

        callService.relay(b.id(), new SignalRequest(callId, "ca", "offer", "{\"sdp\":\"x\"}"));
        CallEvent signal = lastEvent(a.id(), "signal");
        assertThat(signal.signalType()).isEqualTo("offer");
        assertThat(signal.clientId()).isEqualTo("cb");

        assertThatThrownBy(() -> callService.relay(stranger.id(), new SignalRequest(callId, "ca", "offer", "x")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> callService.relay(b.id(), new SignalRequest(callId, "ca", "bogus", "x")))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> callService.join(stranger.id(), "ss", callId, "cs"))
                .isInstanceOf(NotFoundException.class);

        callService.leave(b.id(), callId, "cb");
        assertThat(lastEvent(a.id(), "ended").callId()).isEqualTo(callId);

        callService.start(a.id(), "sa", dm(b.id(), "ca2"));
        assertThat(lastEvent(b.id(), "incoming").callId()).isNotEqualTo(callId);
    }

    @Test
    void recusarEncerraLigacaoDireta() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        befriend(a, b);

        callService.start(a.id(), "sa", dm(b.id(), "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        callService.decline(b.id(), callId);

        assertThat(lastEvent(a.id(), "declined").userId()).isEqualTo(b.id());
        assertThat(lastEvent(a.id(), "ended").reason()).isEqualTo("declined");
        assertThat(lastEvent(b.id(), "dismissed").callId()).isEqualTo(callId);
        callService.join(b.id(), "sb", callId, "cb");
        assertThat(callService.activeFor(b.id())).isEmpty();
        assertThat(lastEvent(b.id(), "ended").callId()).isEqualTo(callId);
    }

    @Test
    void usuarioEmCallNaoPodeIniciarOutraEPessoaOcupadaEhRecusada() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        TestUser c = newUser();
        befriend(a, b);
        befriend(c, b);

        callService.start(a.id(), "sa", dm(b.id(), "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        callService.join(b.id(), "sb", callId, "cb");

        assertThatThrownBy(() -> callService.start(a.id(), "sa", dm(b.id(), "ca2")))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> callService.start(c.id(), "sc", dm(b.id(), "cc")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void grupoAceitaNoMaximoCincoPessoasEMembroForaNaoEntra() throws Exception {
        TestUser owner = newUser();
        List<TestUser> members = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TestUser member = newUser();
            befriend(owner, member);
            members.add(member);
        }
        TestUser outsider = newUser();
        UUID groupId = createGroup(owner, members);

        callService.start(owner.id(), "so", new StartCallRequest(null, groupId, "co"));
        UUID callId = lastEvent(members.get(0).id(), "incoming").callId();
        assertThat(lastEvent(members.get(0).id(), "incoming").groupId()).isEqualTo(groupId);

        for (int i = 0; i < 4; i++) {
            callService.join(members.get(i).id(), "s" + i, callId, "c" + i);
        }
        assertThatThrownBy(() -> callService.join(members.get(4).id(), "s4", callId, "c4"))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> callService.join(outsider.id(), "sx", callId, "cx"))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> callService.start(outsider.id(), "sx", new StartCallRequest(null, groupId, "cx")))
                .isInstanceOf(NotFoundException.class);

        assertThat(callService.activeFor(members.get(4).id())).hasSize(1);
        assertThat(callService.activeFor(owner.id())).isEmpty();

        callService.leave(members.get(0).id(), callId, "c0");
        assertThat(lastEvent(owner.id(), "left").clientId()).isEqualTo("c0");
        callService.join(members.get(4).id(), "s4", callId, "c4");
        assertThat(lastEvent(members.get(4).id(), "state").participants()).hasSize(5);
    }

    @Test
    void segundaAbaDoMesmoUsuarioNaoEntraNaMesmaCall() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        befriend(a, b);

        callService.start(a.id(), "sa", dm(b.id(), "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        callService.join(b.id(), "sb", callId, "cb");

        assertThatThrownBy(() -> callService.join(b.id(), "sb2", callId, "cb-outra-aba"))
                .isInstanceOf(ConflictException.class);

        callService.join(b.id(), "sb-reconectado", callId, "cb");
        assertThat(lastEvent(b.id(), "state").participants()).hasSize(2);
    }
}
