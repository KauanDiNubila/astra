package com.astra.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.user.application.port.in.OAuthLogInCommand;
import com.astra.user.application.port.in.OAuthLogInUseCase;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleOidcUserServiceTest {

    private static final String EMAIL = "vitima@astra.local";

    private final OAuthLogInUseCase oAuthLogIn = mock(OAuthLogInUseCase.class);
    private final GoogleOidcUserService service = new GoogleOidcUserService(oAuthLogIn);

    private static OidcUser googleUser(Object emailVerified) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google-123");
        claims.put("email", EMAIL);
        claims.put("name", "Pessoa");
        if (emailVerified != null) {
            claims.put("email_verified", emailVerified);
        }
        OidcIdToken token = new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(60), claims);
        return new DefaultOidcUser(List.of(), token);
    }

    @Test
    void emailVerificadoEntra() {
        UUID userId = UUID.randomUUID();
        when(oAuthLogIn.logIn(new OAuthLogInCommand("google", "google-123", EMAIL, "Pessoa"))).thenReturn(userId);

        OidcUser user = service.toAstraUser("google", googleUser(true));

        assertThat(((AstraOAuthPrincipal) user).getUserId()).isEqualTo(userId);
    }

    @Test
    void emailNaoVerificadoNaoVinculaConta() {
        assertThatThrownBy(() -> service.toAstraUser("google", googleUser(false)))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verify(oAuthLogIn, never()).logIn(any());
    }

    @Test
    void semInformacaoDeVerificacaoNaoVinculaConta() {
        assertThatThrownBy(() -> service.toAstraUser("google", googleUser(null)))
                .isInstanceOf(OAuth2AuthenticationException.class);
        verify(oAuthLogIn, never()).logIn(any());
    }
}
