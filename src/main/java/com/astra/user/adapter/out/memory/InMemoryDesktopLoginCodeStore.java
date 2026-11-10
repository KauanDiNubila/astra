package com.astra.user.adapter.out.memory;

import com.astra.user.application.port.out.DesktopLoginCodeStorePort;
import com.astra.user.domain.DesktopLoginCode;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class InMemoryDesktopLoginCodeStore implements DesktopLoginCodeStorePort {

    private static final int CLEANUP_THRESHOLD = 500;

    private final Map<String, DesktopLoginCode> codes = new ConcurrentHashMap<>();
    private final Clock clock;

    InMemoryDesktopLoginCodeStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void save(String code, DesktopLoginCode loginCode) {
        if (codes.size() > CLEANUP_THRESHOLD) {
            codes.values().removeIf(pending -> pending.isExpired(clock.instant()));
        }
        codes.put(code, loginCode);
    }

    @Override
    public Optional<DesktopLoginCode> take(String code) {
        return Optional.ofNullable(codes.remove(code));
    }
}
