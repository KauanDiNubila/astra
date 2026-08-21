package com.astra.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PasswordBreachChecker {

    private static final Logger log = LoggerFactory.getLogger(PasswordBreachChecker.class);

    private final RestClient restClient;

    public PasswordBreachChecker() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.pwnedpasswords.com")
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isBreached(String password) {
        try {
            String sha1 = sha1Hex(password);
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);
            String body = restClient.get()
                    .uri("/range/{prefix}", prefix)
                    .retrieve()
                    .body(String.class);
            return body != null && body.lines().anyMatch(line -> line.startsWith(suffix + ":"));
        } catch (Exception ex) {
            log.warn("Falha ao consultar HaveIBeenPwned, seguindo sem bloquear o cadastro", ex);
            return false;
        }
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
