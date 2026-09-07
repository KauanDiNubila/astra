package com.astra.shared.security;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class AstraOAuth2User implements OAuth2User, AstraOAuthPrincipal {

    private final UUID userId;
    private final OAuth2User delegate;

    public AstraOAuth2User(UUID userId, OAuth2User delegate) {
        this.userId = userId;
        this.delegate = delegate;
    }

    @Override
    public UUID getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
