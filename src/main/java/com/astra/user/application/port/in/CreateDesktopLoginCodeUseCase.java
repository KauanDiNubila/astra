package com.astra.user.application.port.in;

import java.util.UUID;

public interface CreateDesktopLoginCodeUseCase {

    String create(UUID userId, String challenge);
}
