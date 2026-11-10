package com.astra.github.adapter.out.token;

import com.astra.github.application.port.out.IssueConnectStatePort;
import com.astra.github.application.port.out.VerifyConnectStatePort;
import com.astra.shared.crypto.JwtService;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ConnectStateTokenAdapter implements IssueConnectStatePort, VerifyConnectStatePort {

    private static final String CONNECT_PURPOSE = "github_connect";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final JwtService jwtService;

    ConnectStateTokenAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String issue(UUID userId) {
        return jwtService.generatePurposeToken(userId, CONNECT_PURPOSE, STATE_TTL);
    }

    @Override
    public UUID verify(String state) {
        return jwtService.extractPurposeUserId(state, CONNECT_PURPOSE);
    }
}
