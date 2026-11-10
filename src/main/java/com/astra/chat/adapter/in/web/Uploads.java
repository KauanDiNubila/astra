package com.astra.chat.adapter.in.web;

import com.astra.chat.application.port.in.ImageUpload;
import com.astra.chat.application.port.in.ImageResponse;
import com.astra.shared.exception.ConflictException;
import java.io.IOException;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

final class Uploads {

    private Uploads() {
    }

    static ImageUpload read(MultipartFile file) {
        try {
            return new ImageUpload(file.getContentType(), file.getBytes());
        } catch (IOException ex) {
            throw new ConflictException("Não foi possível ler o arquivo");
        }
    }

    static ResponseEntity<byte[]> image(ImageResponse image) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.bytes());
    }
}
