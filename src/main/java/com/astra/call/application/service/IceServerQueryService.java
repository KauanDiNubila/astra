package com.astra.call.application.service;

import com.astra.call.application.port.in.GetIceServersQuery;
import com.astra.call.application.port.in.IceServersResponse;
import com.astra.call.application.port.in.IceServersResponse.IceServer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class IceServerQueryService implements GetIceServersQuery {

    private static final Duration CREDENTIAL_TTL = Duration.ofHours(12);
    private static final String PUBLIC_STUN = "stun:stun.l.google.com:19302";

    private final String turnSecret;
    private final List<String> turnUrls;
    private final Clock clock;

    IceServerQueryService(@Value("${astra.call.turn-secret}") String turnSecret,
                          @Value("${astra.call.turn-urls:}") List<String> turnUrls, Clock clock) {
        this.turnSecret = turnSecret;
        this.turnUrls = turnUrls.stream().map(String::trim).filter(url -> !url.isEmpty()).toList();
        this.clock = clock;
    }

    @Override
    public IceServersResponse iceServers(UUID userId) {
        List<IceServer> servers = new ArrayList<>();
        servers.add(new IceServer(List.of(PUBLIC_STUN), null, null));
        if (!turnUrls.isEmpty()) {
            String username = clock.instant().plus(CREDENTIAL_TTL).getEpochSecond() + ":" + userId;
            servers.add(new IceServer(turnUrls, username, credentialFor(username)));
        }
        return new IceServersResponse(servers);
    }

    private String credentialFor(String username) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(turnSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            return Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HmacSHA1 indisponível", ex);
        }
    }
}
