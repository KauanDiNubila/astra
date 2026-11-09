package com.astra.user.application.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {

    void delete(UUID actorId, UUID targetId);
}
