package com.astra.shared.security;

import com.astra.user.application.port.in.OAuthLogInCommand;
import com.astra.user.application.port.in.OAuthLogInUseCase;
import java.util.UUID;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOidcUserService extends OidcUserService {

    private final OAuthLogInUseCase oAuthLogInUseCase;

    public GoogleOidcUserService(OAuthLogInUseCase oAuthLogInUseCase) {
        this.oAuthLogInUseCase = oAuthLogInUseCase;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return toAstraUser(userRequest.getClientRegistration().getRegistrationId(), super.loadUser(userRequest));
    }

    OidcUser toAstraUser(String provider, OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException("Conta do Google sem e-mail verificado");
        }
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : email;

        UUID userId = oAuthLogInUseCase.logIn(new OAuthLogInCommand(provider, oidcUser.getSubject(), email, name));
        return new AstraOidcUser(userId, oidcUser);
    }
}
