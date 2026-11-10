package com.astra.call;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astra.TestcontainersConfiguration;
import com.astra.call.application.port.in.DeclineCallUseCase;
import com.astra.call.application.port.in.JoinCallUseCase;
import com.astra.call.application.port.in.LeaveCallUseCase;
import com.astra.call.application.port.in.ListActiveCallsQuery;
import com.astra.call.application.port.in.RelaySignalCommand;
import com.astra.call.application.port.in.RelaySignalUseCase;
import com.astra.call.application.port.in.StartCallCommand;
import com.astra.call.application.port.in.StartCallUseCase;
import com.astra.call.application.port.out.NotifyCallPort;
import com.astra.call.domain.CallEvent;
import com.astra.call.domain.CallNotice;
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
    private StartCallUseCase startCall;

    @Autowired
    private JoinCallUseCase joinCall;

    @Autowired
    private DeclineCallUseCase declineCall;

    @Autowired
    private LeaveCallUseCase leaveCall;

    @Autowired
    private RelaySignalUseCase relaySignal;

    @Autowired
    private ListActiveCallsQuery listActiveCalls;

    @MockitoSpyBean
    private NotifyCallPort notifyCallPort;

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

    @SuppressWarnings("unchecked")
    private List<CallEvent> eventsFor(UUID userId) {
        List<CallEvent> events = new ArrayList<>();
        Mockito.mockingDetails(notifyCallPort).getInvocations().forEach(invocation -> {
            if (invocation.getMethod().getName().equals("send")) {
                for (CallNotice notice : (List<CallNotice>) invocation.getArguments()[0]) {
                    if (notice.to().equals(userId)) {
                        events.add(notice.event());
                    }
                }
            }
        });
        return events;
    }

    private CallEvent lastEvent(UUID userId, String type) {
        List<CallEvent> events = eventsFor(userId).stream().filter(e -> e.type().equals(type)).toList();
        assertThat(events).as("evento %s para %s", type, userId).isNotEmpty();
        return events.get(events.size() - 1);
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

        assertThatThrownBy(() -> startCall.start(new StartCallCommand(a.id(), "s1", stranger.id(), null, "ca")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void ligacaoDiretaToca_atende_trocaSinalEEncerra() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        TestUser stranger = newUser();
        befriend(a, b);

        startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca"));
        CallEvent incoming = lastEvent(b.id(), "incoming");
        assertThat(incoming.userId()).isEqualTo(a.id());
        UUID callId = incoming.callId();

        joinCall.join(b.id(), "sb", callId, "cb");
        assertThat(lastEvent(a.id(), "joined").clientId()).isEqualTo("cb");
        assertThat(lastEvent(b.id(), "state").participants()).hasSize(2);

        relaySignal.relay(new RelaySignalCommand(b.id(), callId, "ca", "offer", "{\"sdp\":\"x\"}"));
        CallEvent signal = lastEvent(a.id(), "signal");
        assertThat(signal.signalType()).isEqualTo("offer");
        assertThat(signal.clientId()).isEqualTo("cb");

        assertThatThrownBy(() -> relaySignal.relay(new RelaySignalCommand(stranger.id(), callId, "ca", "offer", "x")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> relaySignal.relay(new RelaySignalCommand(b.id(), callId, "ca", "bogus", "x")))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> joinCall.join(stranger.id(), "ss", callId, "cs"))
                .isInstanceOf(NotFoundException.class);

        leaveCall.leave(b.id(), callId, "cb");
        assertThat(lastEvent(a.id(), "ended").callId()).isEqualTo(callId);

        startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca2"));
        assertThat(lastEvent(b.id(), "incoming").callId()).isNotEqualTo(callId);
    }

    @Test
    void recusarEncerraLigacaoDireta() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        befriend(a, b);

        startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        declineCall.decline(b.id(), callId);

        assertThat(lastEvent(a.id(), "declined").userId()).isEqualTo(b.id());
        assertThat(lastEvent(a.id(), "ended").reason()).isEqualTo("declined");
        assertThat(lastEvent(b.id(), "dismissed").callId()).isEqualTo(callId);
        joinCall.join(b.id(), "sb", callId, "cb");
        assertThat(listActiveCalls.activeFor(b.id())).isEmpty();
        assertThat(lastEvent(b.id(), "ended").callId()).isEqualTo(callId);
    }

    @Test
    void usuarioEmCallNaoPodeIniciarOutraEPessoaOcupadaEhRecusada() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        TestUser c = newUser();
        befriend(a, b);
        befriend(c, b);

        startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        joinCall.join(b.id(), "sb", callId, "cb");

        assertThatThrownBy(() -> startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca2")))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> startCall.start(new StartCallCommand(c.id(), "sc", b.id(), null, "cc")))
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

        startCall.start(new StartCallCommand(owner.id(), "so", null, groupId, "co"));
        UUID callId = lastEvent(members.get(0).id(), "incoming").callId();
        assertThat(lastEvent(members.get(0).id(), "incoming").groupId()).isEqualTo(groupId);

        for (int i = 0; i < 4; i++) {
            joinCall.join(members.get(i).id(), "s" + i, callId, "c" + i);
        }
        assertThatThrownBy(() -> joinCall.join(members.get(4).id(), "s4", callId, "c4"))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> joinCall.join(outsider.id(), "sx", callId, "cx"))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> startCall.start(new StartCallCommand(outsider.id(), "sx", null, groupId, "cx")))
                .isInstanceOf(NotFoundException.class);

        assertThat(listActiveCalls.activeFor(members.get(4).id())).hasSize(1);
        assertThat(listActiveCalls.activeFor(owner.id())).isEmpty();

        leaveCall.leave(members.get(0).id(), callId, "c0");
        assertThat(lastEvent(owner.id(), "left").clientId()).isEqualTo("c0");
        joinCall.join(members.get(4).id(), "s4", callId, "c4");
        assertThat(lastEvent(members.get(4).id(), "state").participants()).hasSize(5);
    }

    @Test
    void segundaAbaDoMesmoUsuarioNaoEntraNaMesmaCall() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        befriend(a, b);

        startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        joinCall.join(b.id(), "sb", callId, "cb");

        assertThatThrownBy(() -> joinCall.join(b.id(), "sb2", callId, "cb-outra-aba"))
                .isInstanceOf(ConflictException.class);

        joinCall.join(b.id(), "sb-reconectado", callId, "cb");
        assertThat(lastEvent(b.id(), "state").participants()).hasSize(2);
    }

    @Test
    void grupoContinuaAteOUltimoSairERecusaNoGrupoNaoEncerra() throws Exception {
        TestUser owner = newUser();
        TestUser m1 = newUser();
        TestUser m2 = newUser();
        befriend(owner, m1);
        befriend(owner, m2);
        UUID groupId = createGroup(owner, List.of(m1, m2));

        startCall.start(new StartCallCommand(owner.id(), "so", null, groupId, "co"));
        UUID callId = lastEvent(m1.id(), "incoming").callId();
        assertThat(lastEvent(m1.id(), "incoming").groupName()).isEqualTo("Estudo");

        declineCall.decline(m1.id(), callId);
        assertThat(lastEvent(owner.id(), "declined").userId()).isEqualTo(m1.id());
        assertThat(lastEvent(m1.id(), "dismissed").callId()).isEqualTo(callId);
        assertThat(eventsFor(owner.id())).noneMatch(e -> e.type().equals("ended"));

        startCall.start(new StartCallCommand(m2.id(), "s2", null, groupId, "c2"));
        assertThat(lastEvent(m2.id(), "state").participants()).hasSize(2);

        leaveCall.leave(owner.id(), callId, "outra-aba");
        assertThat(eventsFor(m2.id())).noneMatch(e -> e.type().equals("left"));
        leaveCall.leave(owner.id(), callId, "co");
        assertThat(lastEvent(m2.id(), "left").userId()).isEqualTo(owner.id());
        assertThat(eventsFor(m2.id())).noneMatch(e -> e.type().equals("ended"));

        leaveCall.leave(m2.id(), callId, "c2");
        assertThat(lastEvent(m1.id(), "ended").reason()).isEqualTo("left");
        assertThat(lastEvent(owner.id(), "ended").callId()).isEqualTo(callId);
    }

    @Test
    void pedidoInvalidoESinaisForaDaRegra() throws Exception {
        TestUser a = newUser();
        TestUser b = newUser();
        befriend(a, b);

        assertThatThrownBy(() -> startCall.start(new StartCallCommand(a.id(), "sa", null, null, "ca")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Informe um amigo ou um grupo");
        assertThatThrownBy(() -> startCall.start(new StartCallCommand(a.id(), "sa", a.id(), null, "ca")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Não é possível ligar para si mesmo");

        startCall.start(new StartCallCommand(a.id(), "sa", b.id(), null, "ca"));
        UUID callId = lastEvent(b.id(), "incoming").callId();
        joinCall.join(b.id(), "sb", callId, "cb");

        assertThatThrownBy(() -> relaySignal.relay(new RelaySignalCommand(a.id(), callId, "ninguem", "ice", "x")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Participante não encontrado");
        for (int i = 0; i < 399; i++) {
            relaySignal.relay(new RelaySignalCommand(a.id(), callId, "cb", "ice", "x"));
        }
        assertThatThrownBy(() -> relaySignal.relay(new RelaySignalCommand(a.id(), callId, "cb", "ice", "x")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Muitos sinais em pouco tempo");
        leaveCall.leave(a.id(), callId, "ca");
    }

    @Test
    void chamadaDeGrupoAbertaApareceNaApiParaQuemAindaNaoEntrou() throws Exception {
        TestUser owner = newUser();
        TestUser member = newUser();
        befriend(owner, member);
        UUID groupId = createGroup(owner, List.of(member));

        startCall.start(new StartCallCommand(owner.id(), "so", null, groupId, "co"));

        mockMvc.perform(get("/call/active").header("Authorization", "Bearer " + member.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.groupId == '" + groupId + "')].participantCount").value(1));
        mockMvc.perform(get("/call/active").header("Authorization", "Bearer " + owner.token()))
                .andExpect(jsonPath("$[?(@.groupId == '" + groupId + "')]").isEmpty());
        mockMvc.perform(get("/call/active")).andExpect(status().isUnauthorized());

        leaveCall.leave(owner.id(), lastEvent(member.id(), "incoming").callId(), "co");
    }
}
