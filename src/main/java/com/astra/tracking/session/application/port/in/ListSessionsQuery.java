package com.astra.tracking.session.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListSessionsQuery {

    List<SessionResponse> list(UUID userId, int page, int size);
}
