package com.astra.call.domain;

import java.util.UUID;

public record CallNotice(UUID to, CallEvent event) {
}
