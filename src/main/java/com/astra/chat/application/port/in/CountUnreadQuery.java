package com.astra.chat.application.port.in;

import java.util.UUID;

public interface CountUnreadQuery {

    long direct(UUID userId);

    long groups(UUID userId);
}
