package com.astra.user.application.port.in;

import java.util.Objects;

public record OAuthLogInCommand(String provider, String providerUserId, String email, String name) {

    public OAuthLogInCommand {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(providerUserId);
        Objects.requireNonNull(email);
        Objects.requireNonNull(name);
    }
}
