package com.astra.user.application.port.out;

import com.astra.user.domain.RefreshToken;
import java.util.Optional;

public interface LoadRefreshTokenPort {

    Optional<RefreshToken> loadByHash(String tokenHash);
}
