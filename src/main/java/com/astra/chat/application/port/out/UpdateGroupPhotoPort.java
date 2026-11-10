package com.astra.chat.application.port.out;

import com.astra.chat.domain.ChatImage;
import java.util.UUID;

public interface UpdateGroupPhotoPort {

    void update(UUID groupId, ChatImage photo);
}
