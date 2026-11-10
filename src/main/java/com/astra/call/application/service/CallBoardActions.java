package com.astra.call.application.service;

import com.astra.call.application.port.out.CallBoardPort;
import com.astra.call.application.port.out.NotifyCallPort;
import com.astra.call.application.port.out.ScheduleCallTaskPort;
import com.astra.call.domain.CallBoard;
import com.astra.call.domain.CallNotice;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
class CallBoardActions {

    private final CallBoardPort callBoardPort;
    private final NotifyCallPort notifyCallPort;
    private final ScheduleCallTaskPort scheduleCallTaskPort;

    CallBoardActions(CallBoardPort callBoardPort, NotifyCallPort notifyCallPort,
                     ScheduleCallTaskPort scheduleCallTaskPort) {
        this.callBoardPort = callBoardPort;
        this.notifyCallPort = notifyCallPort;
        this.scheduleCallTaskPort = scheduleCallTaskPort;
    }

    void apply(Function<CallBoard, List<CallNotice>> transition) {
        apply(transition, Function.identity());
    }

    <T> T apply(Function<CallBoard, T> transition, Function<T, List<CallNotice>> noticesOf) {
        return callBoardPort.change(board -> {
            T result = transition.apply(board);
            notifyCallPort.send(noticesOf.apply(result));
            return result;
        });
    }

    <T> T read(Function<CallBoard, T> query) {
        return callBoardPort.change(query);
    }

    void applyLater(Duration delay, Function<CallBoard, List<CallNotice>> transition) {
        scheduleCallTaskPort.schedule(delay, () -> apply(transition));
    }
}
