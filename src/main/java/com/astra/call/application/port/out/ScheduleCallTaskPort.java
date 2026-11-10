package com.astra.call.application.port.out;

import java.time.Duration;

public interface ScheduleCallTaskPort {

    void schedule(Duration delay, Runnable task);
}
