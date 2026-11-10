package com.astra.chat.application.port.in;

import java.util.List;
import java.util.UUID;

public record PinChange(MessageResponse message, List<UUID> audience) {
}
