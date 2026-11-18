package com.astra.user.application.port.out;

public interface SendPasswordResetEmailPort {

    void sendPasswordReset(String email, String rawToken);
}
