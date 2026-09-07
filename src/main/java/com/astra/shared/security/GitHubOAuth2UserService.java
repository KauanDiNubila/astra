package com.astra.shared.security;

import com.astra.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final RestClient restClient = RestClient.create();

    public GitHubOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = String.valueOf(oAuth2User.getAttributes().get("id"));

        String email = (String) oAuth2User.getAttributes().get("email");
        if (email == null) {
            email = fetchPrimaryVerifiedEmail(userRequest.getAccessToken().getTokenValue());
        }
        if (email == null) {
            throw new OAuth2AuthenticationException("Conta do GitHub sem e-mail verificado disponível");
        }
        String name = (String) oAuth2User.getAttributes().getOrDefault("name", email);

        UUID userId = userService.oauthLogin(provider, providerUserId, email, name);
        return new AstraOAuth2User(userId, oAuth2User);
    }

    private String fetchPrimaryVerifiedEmail(String accessToken) {
        List<Map<String, Object>> emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (emails == null) {
            return null;
        }
        return emails.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.get("primary")) && Boolean.TRUE.equals(entry.get("verified")))
                .map(entry -> (String) entry.get("email"))
                .findFirst()
                .orElse(null);
    }
}
