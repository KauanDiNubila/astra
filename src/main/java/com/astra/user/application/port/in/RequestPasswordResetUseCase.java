package com.astra.user.application.port.in;

public interface RequestPasswordResetUseCase {

    void request(String email);
}
