package com.astra.tracking.session.application.port.out;

import com.astra.tracking.session.domain.Session;

public interface CreateSessionPort {

    Session create(Session session);
}
