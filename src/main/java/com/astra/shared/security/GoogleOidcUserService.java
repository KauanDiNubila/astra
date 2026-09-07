package com.astra.shared.security;

import com.astra.user.UserService;
import java.util.UUID;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleOidcUserService extends OidcUserService {

    private final UserService userService;

    public GoogleOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = oidcUser.getEmail();
        if (email == null) {
            throw new OAuth2AuthenticationException("Conta do Google sem e-mail verificado");
        }
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : email;

        UUID userId = userService.oauthLogin(provider, oidcUser.getSubject(), email, name);
        return new AstraOidcUser(userId, oidcUser);
    }
}
