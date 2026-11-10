package com.astra.chat.application.port.in;

import java.util.Objects;

public record ImageUpload(String contentType, byte[] bytes) {

    public ImageUpload {
        Objects.requireNonNull(bytes);
    }
}
