package com.astra.user.application.port.in;

import java.util.Optional;
import java.util.UUID;

public interface GetAvatarQuery {

    Optional<AvatarResponse> avatar(UUID userId);
}
