package com.astra.user.application.port.out;

import com.astra.user.domain.RefreshToken;

public interface CreateRefreshTokenPort {

    void create(RefreshToken token);
}
