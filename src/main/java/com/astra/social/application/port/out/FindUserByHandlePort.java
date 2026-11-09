package com.astra.social.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface FindUserByHandlePort {

    Optional<UUID> findIdByHandle(String name, String tag);
}
