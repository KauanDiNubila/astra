package com.astra.tracking.session.application.port.out;

import java.util.UUID;

public interface DeleteSessionPort {

    void delete(UUID sessionId);
}
