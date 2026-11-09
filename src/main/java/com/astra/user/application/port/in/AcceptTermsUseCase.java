package com.astra.user.application.port.in;

import java.util.UUID;

public interface AcceptTermsUseCase {

    UserResponse accept(UUID userId);
}
