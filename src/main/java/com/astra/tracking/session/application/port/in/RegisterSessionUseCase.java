package com.astra.tracking.session.application.port.in;

public interface RegisterSessionUseCase {

    SessionResponse register(RegisterSessionCommand command);
}
