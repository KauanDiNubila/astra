package com.astra.chat.domain;

import java.util.Objects;

public record ChatImage(String contentType, byte[] bytes) {

    public static final int MAX_BYTES = 2 * 1024 * 1024;

    public ChatImage {
        Objects.requireNonNull(contentType);
        Objects.requireNonNull(bytes);
    }

    public static ChatImage of(String contentType, byte[] bytes) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidChatImageException("Arquivo precisa ser uma imagem");
        }
        if (bytes.length > MAX_BYTES) {
            throw new InvalidChatImageException("Imagem precisa ter até 2MB");
        }
        return new ChatImage(contentType, bytes);
    }
}
