package com.astra.chat.application.port.out;

import com.astra.chat.domain.ChatImage;
import java.util.Optional;
import java.util.UUID;

public interface LoadGroupPhotoPort {

    Optional<ChatImage> load(UUID groupId);
}
