package com.astra.user.application.port.in;

public interface RevokeRefreshTokenUseCase {

    void revoke(String rawToken);
}
