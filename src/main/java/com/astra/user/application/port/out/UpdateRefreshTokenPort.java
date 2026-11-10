package com.astra.user.application.port.out;

import com.astra.user.domain.RefreshToken;

public interface UpdateRefreshTokenPort {

    void update(RefreshToken token);
}
