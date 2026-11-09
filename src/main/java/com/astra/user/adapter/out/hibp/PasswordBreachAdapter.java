package com.astra.user.adapter.out.hibp;

import com.astra.user.application.port.out.CheckPasswordBreachPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class PasswordBreachAdapter implements CheckPasswordBreachPort {

    private static final Logger log = LoggerFactory.getLogger(PasswordBreachAdapter.class);

    private final RestClient restClient;

    PasswordBreachAdapter() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.pwnedpasswords.com")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public boolean isBreached(String password) {
        try {
            String sha1 = sha1Hex(password);
            String suffix = sha1.substring(5);
            String body = restClient.get()
                    .uri("/range/{prefix}", sha1.substring(0, 5))
                    .retrieve()
                    .body(String.class);
            return body != null && body.lines().anyMatch(line -> line.startsWith(suffix + ":"));
        } catch (Exception ex) {
            log.warn("Falha ao consultar HaveIBeenPwned, seguindo sem bloquear o cadastro", ex);
            return false;
        }
    }

    private static String sha1Hex(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
