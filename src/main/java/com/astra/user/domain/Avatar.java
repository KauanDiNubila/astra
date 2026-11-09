package com.astra.user.domain;

import com.astra.shared.exception.ConflictException;
import java.util.Objects;

public record Avatar(byte[] bytes, String contentType) {

    public static final int MAX_BYTES = 2 * 1024 * 1024;

    public static Avatar of(String contentType, byte[] bytes) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ConflictException("Arquivo precisa ser uma imagem");
        }
        if (bytes.length > MAX_BYTES) {
            throw new ConflictException("Imagem precisa ter até 2MB");
        }
        return new Avatar(bytes, contentType);
    }

    public Avatar {
        Objects.requireNonNull(bytes);
        Objects.requireNonNull(contentType);
    }
}
