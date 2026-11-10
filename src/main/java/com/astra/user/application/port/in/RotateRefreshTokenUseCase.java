package com.astra.user.application.port.in;

import java.util.UUID;

public interface RotateRefreshTokenUseCase {

    UUID rotate(String rawToken);
}
