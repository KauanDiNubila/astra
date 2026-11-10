package com.astra.call.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CallBoardTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BETO = UUID.randomUUID();
    private static final UUID CAIO = UUID.randomUUID();
    private static final UUID GROUP = UUID.randomUUID();

    private final CallBoard board = new CallBoard();

    private final CallDirectory directory = new CallDirectory() {
        @Override
        public boolean areFriends(UUID a, UUID b) {
            return !a.equals(CAIO) && !b.equals(CAIO);
        }

        @Override
        public List<UUID> groupMembers(UUID groupId) {
            return List.of(ANA, BETO, CAIO);
        }

        @Override
        public String groupName(UUID groupId) {
            return "Estudo";
        }

        @Override
        public String nameOf(UUID userId) {
            return userId.equals(ANA) ? "Ana" : "Outro";
        }
    };

    private UUID direct() {
        return board.start(ANA, "sa", "ca", BETO, null, directory).ringingCallId().orElseThrow();
    }

    private static List<String> types(List<CallNotice> notices, UUID to) {
        return notices.stream().filter(n -> n.to().equals(to)).map(n -> n.event().type()).toList();
    }

    @Test
    void ligacaoDiretaAvisaQuemLigaEQuemRecebe() {
        CallBoard.StartResult result = board.start(ANA, "sa", "ca", BETO, null, directory);

        assertThat(types(result.notices(), ANA)).containsExactly("started");
        assertThat(types(result.notices(), BETO)).containsExactly("incoming");
        assertThat(result.notices().get(1).event().userName()).isEqualTo("Ana");
        assertThatThrownBy(() -> board.start(ANA, "sa", "ca2", BETO, null, directory))
                .isInstanceOf(AlreadyInCallException.class);
        assertThatThrownBy(() -> board.start(CAIO, "sc", "cc", ANA, null, directory))
                .isInstanceOf(OnlyFriendsCallException.class);
    }

    @Test
    void toqueSemRespostaExpira() {
        UUID callId = direct();

        assertThat(types(board.expire(callId), BETO)).containsExactly("ended");
        assertThat(board.expire(callId)).isEmpty();
        assertThat(board.start(ANA, "sa", "ca", BETO, null, directory).ringingCallId()).isPresent();
    }

    @Test
    void chamadaAtendidaNaoExpiraMesmoSeAlguemSair() {
        UUID callId = board.start(ANA, "sa", "ca", null, GROUP, directory).ringingCallId().orElseThrow();
        board.join(BETO, "sb", callId, "cb", directory);
        board.leave(BETO, callId, "cb");

        assertThat(board.expire(callId)).isEmpty();
        assertThat(board.activeFor(CAIO)).singleElement().satisfies(a -> assertThat(a.participantCount()).isEqualTo(1));
    }

    @Test
    void quedaDeConexaoSoDerrubaQuemNaoVoltou() {
        UUID callId = direct();
        board.join(BETO, "sb", callId, "cb", directory);
        List<StaleMember> stale = board.membersOnSession("sb");
        assertThat(stale).containsExactly(new StaleMember(callId, BETO, "cb", "sb"));

        board.join(BETO, "sb-novo", callId, "cb", directory);
        assertThat(board.dropIfStale(stale.getFirst())).isEmpty();

        List<CallNotice> dropped = board.dropIfStale(new StaleMember(callId, BETO, "cb", "sb-novo"));
        assertThat(types(dropped, ANA)).containsExactly("left", "ended");
    }

    @Test
    void grupoComCallAbertaEntraNelaEmVezDeAbrirOutra() {
        UUID callId = board.start(ANA, "sa", "ca", null, GROUP, directory).ringingCallId().orElseThrow();

        CallBoard.StartResult second = board.start(BETO, "sb", "cb", null, GROUP, directory);

        assertThat(second.ringingCallId()).isEmpty();
        assertThat(types(second.notices(), BETO)).containsExactly("state");
        assertThat(second.notices().getFirst().event().callId()).isEqualTo(callId);
    }

    @Test
    void sinalValidaTipoLimiteEDestino() {
        UUID callId = direct();
        board.join(BETO, "sb", callId, "cb", directory);

        assertThatThrownBy(() -> board.relay(ANA, callId, "cb", "bogus", "x", 0))
                .isInstanceOf(InvalidSignalException.class);
        for (int i = 0; i < CallBoard.MAX_SIGNALS_PER_WINDOW; i++) {
            assertThat(board.relay(ANA, callId, "cb", "ice", "x", 0)).hasSize(1);
        }
        assertThatThrownBy(() -> board.relay(ANA, callId, "cb", "ice", "x", 0))
                .isInstanceOf(TooManySignalsException.class);
        assertThat(board.relay(ANA, callId, "cb", "ice", "x", CallBoard.SIGNAL_WINDOW_MILLIS + 1))
                .singleElement().satisfies(n -> assertThat(n.event().clientId()).isEqualTo("ca"));
    }
}
