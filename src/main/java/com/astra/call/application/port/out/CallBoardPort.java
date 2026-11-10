package com.astra.call.application.port.out;

import com.astra.call.domain.CallBoard;
import java.util.function.Function;

public interface CallBoardPort {

    <T> T change(Function<CallBoard, T> change);
}
