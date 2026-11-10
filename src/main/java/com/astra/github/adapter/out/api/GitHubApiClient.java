package com.astra.github.adapter.out.api;

import com.astra.github.domain.GitHubApiException;
import com.astra.github.domain.TokenGrant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
class GitHubApiClient {

    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 28800L;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String clientId;
    private final String clientSecret;

    GitHubApiClient(@Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId,
                    @Value("${spring.security.oauth2.client.registration.github.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    TokenGrant exchangeCode(String code, String redirectUri) {
        return requestToken(Map.of("client_id", clientId, "client_secret", clientSecret, "code", code,
                "redirect_uri", redirectUri));
    }

    TokenGrant refreshToken(String refreshToken) {
        return requestToken(Map.of("client_id", clientId, "client_secret", clientSecret,
                "grant_type", "refresh_token", "refresh_token", refreshToken));
    }

    Map<String, Object> fetchViewerProfile(String accessToken) {
        return restClient.get()
                .uri("https://api.github.com/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    JsonNode graphql(String accessToken, String query, Map<String, Object> variables) {
        Map<String, Object> raw;
        try {
            raw = restClient.post()
                    .uri("https://api.github.com/graphql")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .body(Map.of("query", query, "variables", variables))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException e) {
            throw new GitHubApiException("GitHub respondeu " + e.getStatusCode().value() + " na consulta GraphQL", e);
        }
        if (raw == null) {
            throw new GitHubApiException("Resposta vazia da API do GitHub");
        }
        JsonNode response = objectMapper.valueToTree(raw);
        if (response.has("errors") && !response.get("errors").isEmpty()) {
            throw new GitHubApiException("GitHub GraphQL retornou erro: " + response.get("errors"));
        }
        JsonNode data = response.get("data");
        if (data == null || data.isNull()) {
            throw new GitHubApiException("Resposta do GitHub sem campo 'data'");
        }
        return data;
    }

    <T> T restGet(String accessToken, String url, ParameterizedTypeReference<T> type) {
        try {
            return restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw new GitHubApiException("GitHub respondeu " + e.getStatusCode().value() + " em " + url, e);
        }
    }

    private TokenGrant requestToken(Map<String, String> form) {
        Map<String, Object> body = restClient.post()
                .uri(TOKEN_URL)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (body == null || body.get("access_token") == null) {
            String error = body == null ? "resposta vazia" : String.valueOf(body.get("error_description"));
            throw new GitHubApiException("Falha ao obter token do GitHub: " + error);
        }
        Object expiresIn = body.get("expires_in");
        Object refreshExpiresIn = body.get("refresh_token_expires_in");
        return new TokenGrant((String) body.get("access_token"), (String) body.get("refresh_token"),
                expiresIn == null ? DEFAULT_EXPIRES_IN_SECONDS : ((Number) expiresIn).longValue(),
                refreshExpiresIn == null ? null : ((Number) refreshExpiresIn).longValue());
    }
}
