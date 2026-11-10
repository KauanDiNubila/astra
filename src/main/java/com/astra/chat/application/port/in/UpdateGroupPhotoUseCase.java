package com.astra.chat.application.port.in;

import java.util.UUID;

public interface UpdateGroupPhotoUseCase {

    void update(UUID userId, UUID groupId, ImageUpload photo);
}
