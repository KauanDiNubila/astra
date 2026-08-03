package com.astra.shared.security;

import com.astra.shared.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SeedCurrentUserProvider implements CurrentUserProvider {

    private static final UUID SEED_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public UUID currentUserId() {
        return SEED_USER_ID;
    }
}
