package com.astra.user.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface HandlePort {

    boolean isTaken(String name, String tag);

    Optional<UUID> findIdByHandle(String name, String tag);
}
