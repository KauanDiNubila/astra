package com.astra.user;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class UserTagGenerator {

    private static final int MAX_ATTEMPTS = 25;

    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public UserTagGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate(String name) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = "%04d".formatted(random.nextInt(10000));
            if (!userRepository.existsByNameAndTag(name, candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Não foi possível gerar tag única para \"" + name + "\"");
    }

    // Mantém a tag atual se ainda estiver livre pro novo nome, senão gera outra.
    public String generateOrKeep(String newName, String currentTag) {
        if (!userRepository.existsByNameAndTag(newName, currentTag)) {
            return currentTag;
        }
        return generate(newName);
    }
}
