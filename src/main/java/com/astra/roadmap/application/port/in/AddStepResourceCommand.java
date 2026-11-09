package com.astra.roadmap.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record AddStepResourceCommand(UUID userId, UUID stepId, String label, String url) {

    public static final int LABEL_MAX_LENGTH = 160;
    public static final int URL_MAX_LENGTH = 500;

    public AddStepResourceCommand {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(stepId);
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        if (label != null && label.length() > LABEL_MAX_LENGTH) {
            throw new IllegalArgumentException("label must have at most " + LABEL_MAX_LENGTH + " characters");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }
        if (url != null && url.length() > URL_MAX_LENGTH) {
            throw new IllegalArgumentException("url must have at most " + URL_MAX_LENGTH + " characters");
        }
    }
}
