package com.astra.chat.application.port.in;

import java.util.UUID;

public interface GetAttachmentQuery {

    ImageResponse attachment(UUID userId, UUID messageId);
}
