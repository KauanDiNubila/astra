package com.astra.user.application.port.in;

import java.util.UUID;

public interface GetUserQuery {

    UserResponse get(UUID userId);
}
