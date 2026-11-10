package com.astra.call.adapter.out.scheduler;

import com.astra.call.application.port.out.ScheduleCallTaskPort;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
class ExecutorCallScheduler implements ScheduleCallTaskPort {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "call-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void schedule(Duration delay, Runnable task) {
        scheduler.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }
}
