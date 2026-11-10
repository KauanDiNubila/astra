package com.astra.chat.application.port.in;

import java.util.UUID;

public interface GetGroupPhotoQuery {

    ImageResponse photo(UUID userId, UUID groupId);
}
