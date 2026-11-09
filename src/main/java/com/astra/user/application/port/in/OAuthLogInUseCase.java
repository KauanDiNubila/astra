package com.astra.user.application.port.in;

import java.util.UUID;

public interface OAuthLogInUseCase {

    UUID logIn(OAuthLogInCommand command);
}
