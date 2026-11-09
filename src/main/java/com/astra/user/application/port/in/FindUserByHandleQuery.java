package com.astra.user.application.port.in;

import java.util.Optional;
import java.util.UUID;

public interface FindUserByHandleQuery {

    Optional<UUID> findIdByHandle(String name, String tag);
}
