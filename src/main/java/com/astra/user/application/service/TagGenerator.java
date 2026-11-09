package com.astra.user.application.service;

import com.astra.user.application.port.out.HandlePort;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
class TagGenerator {

    private static final int MAX_ATTEMPTS = 25;

    private final HandlePort handlePort;
    private final SecureRandom random = new SecureRandom();

    TagGenerator(HandlePort handlePort) {
        this.handlePort = handlePort;
    }

    String generate(String name) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = "%04d".formatted(random.nextInt(10000));
            if (!handlePort.isTaken(name, candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Não foi possível gerar tag única para \"" + name + "\"");
    }

    String generateOrKeep(String newName, String currentTag) {
        return handlePort.isTaken(newName, currentTag) ? generate(newName) : currentTag;
    }
}
