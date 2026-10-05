package com.astra.shared.security;

import com.astra.user.service.DesktopLoginService;
import com.astra.user.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final DesktopLoginService desktopLoginService;
    private final String frontendUrl;

    public OAuthAuthenticationSuccessHandler(RefreshTokenService refreshTokenService,
            DesktopLoginService desktopLoginService, @Value("${astra.frontend-url}") String frontendUrl) {
        this.refreshTokenService = refreshTokenService;
        this.desktopLoginService = desktopLoginService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        AstraOAuthPrincipal principal = (AstraOAuthPrincipal) authentication.getPrincipal();

        HttpSession session = request.getSession(false);
        String challenge = session == null ? null
                : (String) session.getAttribute(DesktopLoginService.SESSION_ATTRIBUTE);
        if (challenge != null) {
            session.removeAttribute(DesktopLoginService.SESSION_ATTRIBUTE);
            String code = desktopLoginService.createCode(principal.getUserId(), challenge);
            writeDesktopReturnPage(response, code);
            return;
        }

        refreshTokenService.issueAndAttachCookie(principal.getUserId(), response);
        response.sendRedirect(frontendUrl + "/dashboard");
    }

    private void writeDesktopReturnPage(HttpServletResponse response, String code) throws IOException {
        String link = "astra://auth?code=" + code;
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Astra</title></head>"
                + "<body style=\"margin:0;min-height:100vh;display:grid;place-items:center;background:#0a0a0a;"
                + "color:#fafafa;font-family:system-ui,sans-serif;text-align:center\"><main>"
                + "<h1>Login concluído</h1><p>Voltando para o Astra…</p>"
                + "<p><a style=\"color:#fafafa\" href=\"" + link + "\">Abrir o Astra</a></p>"
                + "<p style=\"opacity:.6\">Você já pode fechar esta aba.</p></main>"
                + "<script>location.href=\"" + link + "\"</script></body></html>");
    }
}
