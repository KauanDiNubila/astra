package com.astra.call.adapter.out.memory;

import com.astra.call.application.port.out.CallBoardPort;
import com.astra.call.domain.CallBoard;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
class InMemoryCallBoard implements CallBoardPort {

    private final CallBoard board = new CallBoard();

    @Override
    public synchronized <T> T change(Function<CallBoard, T> change) {
        return change.apply(board);
    }
}
