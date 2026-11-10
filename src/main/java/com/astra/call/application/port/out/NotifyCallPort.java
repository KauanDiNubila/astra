package com.astra.call.application.port.out;

import com.astra.call.domain.CallNotice;
import java.util.List;

public interface NotifyCallPort {

    void send(List<CallNotice> notices);
}
