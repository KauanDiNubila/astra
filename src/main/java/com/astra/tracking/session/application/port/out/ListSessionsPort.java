package com.astra.tracking.session.application.port.out;

import com.astra.tracking.session.domain.Session;
import java.util.List;
import java.util.UUID;

public interface ListSessionsPort {

    List<Session> listNewestFirst(UUID userId, int page, int size);
}
