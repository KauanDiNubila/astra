package com.astra.user.application.port.in;

import java.util.UUID;

public interface BanUserUseCase {

    void ban(UUID actorId, UUID targetId);

    void unban(UUID actorId, UUID targetId);
}
