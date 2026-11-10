package com.astra.call.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.call.application.port.in.StartCallCommand;
import com.astra.call.application.port.out.CallBoardPort;
import com.astra.call.application.port.out.CheckFriendshipPort;
import com.astra.call.application.port.out.LoadGroupInfoPort;
import com.astra.call.application.port.out.LoadUserNamePort;
import com.astra.call.application.port.out.NotifyCallPort;
import com.astra.call.application.port.out.ScheduleCallTaskPort;
import com.astra.call.domain.CallBoard;
import com.astra.call.domain.CallNotice;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CallServicesTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BETO = UUID.randomUUID();

    private final CallBoard board = new CallBoard();
    private final List<CallNotice> sent = new ArrayList<>();
    private final List<Duration> delays = new ArrayList<>();
    private final List<Runnable> tasks = new ArrayList<>();

    private final CallBoardPort boardPort = new CallBoardPort() {
        @Override
        public <T> T change(Function<CallBoard, T> change) {
            return change.apply(board);
        }
    };
    private final NotifyCallPort notify = sent::addAll;
    private final ScheduleCallTaskPort scheduler = (delay, task) -> {
        delays.add(delay);
        tasks.add(task);
    };
    private final CallBoardActions actions = new CallBoardActions(boardPort, notify, scheduler);
    private final PortsCallDirectory directory = new PortsCallDirectory(
            (CheckFriendshipPort) (a, b) -> true,
            new LoadGroupInfoPort() {
                @Override
                public List<UUID> memberIds(UUID groupId) {
                    return List.of();
                }

                @Override
                public String name(UUID groupId) {
                    return "";
                }
            },
            (LoadUserNamePort) userId -> "Nome");

    @Test
    void iniciarAvisaEAgendaOFimDoToqueQueEncerraSeNinguemAtender() {
        new StartCallService(actions, directory).start(new StartCallCommand(ANA, "sa", BETO, null, "ca"));

        assertThat(sent).extracting(n -> n.event().type()).containsExactly("started", "incoming");
        assertThat(delays).containsExactly(StartCallService.RING_TIMEOUT);

        sent.clear();
        tasks.getFirst().run();
        assertThat(sent).extracting(n -> n.event().type()).containsExactly("ended", "ended");
        assertThat(sent.getFirst().event().reason()).isEqualTo("no-answer");
    }

    @Test
    void quedaDeConexaoAgendaAChecagemDepoisDaTolerancia() {
        new StartCallService(actions, directory).start(new StartCallCommand(ANA, "sa", BETO, null, "ca"));
        delays.clear();
        tasks.clear();

        new HandleDisconnectService(actions).disconnected("sa");

        assertThat(delays).containsExactly(HandleDisconnectService.DISCONNECT_GRACE);
        sent.clear();
        tasks.getFirst().run();
        assertThat(sent).extracting(n -> n.event().type()).contains("ended");
    }
}
