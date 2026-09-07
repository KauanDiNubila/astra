package com.astra.shared.security;

import com.astra.user.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final String frontendUrl;

    public OAuthAuthenticationSuccessHandler(RefreshTokenService refreshTokenService,
            @Value("${astra.frontend-url}") String frontendUrl) {
        this.refreshTokenService = refreshTokenService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        AstraOAuthPrincipal principal = (AstraOAuthPrincipal) authentication.getPrincipal();
        refreshTokenService.issueAndAttachCookie(principal.getUserId(), response);
        response.sendRedirect(frontendUrl + "/dashboard");
    }
}
