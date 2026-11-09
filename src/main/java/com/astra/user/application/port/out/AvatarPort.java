package com.astra.user.application.port.out;

import com.astra.user.domain.Avatar;
import java.util.Optional;
import java.util.UUID;

public interface AvatarPort {

    Optional<Avatar> load(UUID userId);

    void save(UUID userId, Avatar avatar);
}
