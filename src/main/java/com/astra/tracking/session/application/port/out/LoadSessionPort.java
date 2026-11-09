package com.astra.tracking.session.application.port.out;

import com.astra.tracking.session.domain.Session;
import java.util.Optional;
import java.util.UUID;

public interface LoadSessionPort {

    Optional<Session> load(UUID sessionId);
}
