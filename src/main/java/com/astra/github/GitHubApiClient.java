package com.astra.github;

import com.astra.github.dto.GitHubTokenResponse;
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
public class GitHubApiClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String clientId;
    private final String clientSecret;

    public GitHubApiClient(
            @Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.github.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public GitHubTokenResponse exchangeCode(String code, String redirectUri) {
        return parseTokenResponse(restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}));
    }

    public GitHubTokenResponse refreshToken(String refreshToken) {
        return parseTokenResponse(restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "grant_type", "refresh_token",
                        "refresh_token", refreshToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}));
    }

    @SuppressWarnings("unchecked")
    private GitHubTokenResponse parseTokenResponse(Map<String, Object> body) {
        if (body == null || body.get("access_token") == null) {
            String error = body == null ? "resposta vazia" : String.valueOf(body.get("error_description"));
            throw new GitHubApiException("Falha ao obter token do GitHub: " + error);
        }
        Object expiresIn = body.get("expires_in");
        Object refreshExpiresIn = body.get("refresh_token_expires_in");
        return new GitHubTokenResponse(
                (String) body.get("access_token"),
                (String) body.get("refresh_token"),
                expiresIn == null ? 28800L : ((Number) expiresIn).longValue(),
                refreshExpiresIn == null ? null : ((Number) refreshExpiresIn).longValue());
    }

    public Map<String, Object> fetchViewerProfile(String accessToken) {
        return restClient.get()
                .uri("https://api.github.com/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public JsonNode graphql(String accessToken, String query, Map<String, Object> variables) {
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
        // Convertido pra JsonNode via ObjectMapper direto (nao pelo
        // HttpMessageConverter do RestClient) - Map/List ja e um formato que
        // o RestClient desserializa sem ambiguidade de conversor.
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

    public <T> T restGet(String accessToken, String url, ParameterizedTypeReference<T> type) {
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
}
