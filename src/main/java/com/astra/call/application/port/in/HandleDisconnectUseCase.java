package com.astra.call.application.port.in;

public interface HandleDisconnectUseCase {

    void disconnected(String sessionId);
}
