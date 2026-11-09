package com.astra.user.application.port.out;

import com.astra.user.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {

    Optional<User> load(UUID userId);

    Optional<User> loadByEmail(String email);
}
