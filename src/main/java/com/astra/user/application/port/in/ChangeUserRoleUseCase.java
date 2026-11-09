package com.astra.user.application.port.in;

import java.util.UUID;

public interface ChangeUserRoleUseCase {

    void promote(UUID actorId, UUID targetId);

    void demote(UUID actorId, UUID targetId);
}
